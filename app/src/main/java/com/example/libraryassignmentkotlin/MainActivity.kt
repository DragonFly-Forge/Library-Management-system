package com.example.libraryassignmentkotlin

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val library = LibraryService()

    private lateinit var tvFeedback: TextView
    private lateinit var tvSearchResult: TextView
    private lateinit var tvBooksList: TextView
    private lateinit var tvIssuedList: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        tvFeedback = findViewById(R.id.tvFeedback)
        tvSearchResult = findViewById(R.id.tvSearchResult)
        tvBooksList = findViewById(R.id.tvBooksList)
        tvIssuedList = findViewById(R.id.tvIssuedList)

        val etBookId = findViewById<EditText>(R.id.etBookId)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etAuthor = findViewById<EditText>(R.id.etAuthor)
        val etYear = findViewById<EditText>(R.id.etYear)
        val etCopies = findViewById<EditText>(R.id.etCopies)
        val btnAddBook = findViewById<Button>(R.id.btnAddBook)

        val etSearchQuery = findViewById<EditText>(R.id.etSearchQuery)
        val btnSearch = findViewById<Button>(R.id.btnSearch)

        val etActionBookId = findViewById<EditText>(R.id.etActionBookId)
        val etStudentId = findViewById<EditText>(R.id.etStudentId)
        val btnIssue = findViewById<Button>(R.id.btnIssue)
        val btnReturn = findViewById<Button>(R.id.btnReturn)

        // Task 1: Add Book
        btnAddBook.setOnClickListener {
            val id = etBookId.text.toString().trim().toIntOrNull()
            val title = etTitle.text.toString()
            val author = etAuthor.text.toString()
            val year = etYear.text.toString().trim().toIntOrNull()
            val copies = etCopies.text.toString().trim().toIntOrNull()

            val (success, message) = library.addBook(id, title, author, year, copies)
            showFeedback(success, message)

            if (success) {
                etBookId.text.clear()
                etTitle.text.clear()
                etAuthor.text.clear()
                etYear.text.clear()
                etCopies.text.clear()
                refreshLists()
            }
        }

        // Task 3: Search Book
        btnSearch.setOnClickListener {
            val query = etSearchQuery.text.toString()
            val results = library.searchBooks(query)

            if (query.isBlank()) {
                tvSearchResult.text = "Enter a Book ID or title to search."
                return@setOnClickListener
            }

            if (results.isEmpty()) {
                tvSearchResult.text = "No books found matching '${query.trim()}'."
            } else {
                tvSearchResult.text = results.joinToString("\n\n") { book ->
                    val status = if (book.copies > 0) "Available (${book.copies} copies)" else "Out of Stock"
                    "ID: ${book.id} | ${book.title}\nAuthor: ${book.author} (${book.year})\nStatus: $status"
                }
            }
        }

        // Task 4: Issue Book
        btnIssue.setOnClickListener {
            val bookId = etActionBookId.text.toString().trim().toIntOrNull()
            val studentId = etStudentId.text.toString()

            val (success, message) = library.issueBook(bookId, studentId)
            showFeedback(success, message)

            if (success) {
                refreshLists()
            }
        }

        // Task 5: Return Book
        btnReturn.setOnClickListener {
            val bookId = etActionBookId.text.toString().trim().toIntOrNull()
            val studentId = etStudentId.text.toString()

            val (success, message) = library.returnBook(bookId, studentId)
            showFeedback(success, message)

            if (success) {
                refreshLists()
            }
        }

        refreshLists()
    }

    private fun showFeedback(success: Boolean, message: String) {
        tvFeedback.text = message
        if (success) {
            tvFeedback.setBackgroundColor(Color.parseColor("#DCFCE7"))
            tvFeedback.setTextColor(Color.parseColor("#166534"))
        } else {
            tvFeedback.setBackgroundColor(Color.parseColor("#FEE2E2"))
            tvFeedback.setTextColor(Color.parseColor("#991B1B"))
        }
    }

    // Tasks 2 & 6: Refresh All Books and Currently Issued Books
    private fun refreshLists() {
        val allBooks = library.getAllBooks()
        tvBooksList.text = if (allBooks.isEmpty()) {
            "No books registered yet."
        } else {
            allBooks.joinToString("\n") { b ->
                "#${b.id} | ${b.title} — ${b.author} (${b.year}) | Copies: ${b.copies}"
            }
        }

        val issuedBooks = library.getIssuedBooks()
        tvIssuedList.text = if (issuedBooks.isEmpty()) {
            "No books currently issued."
        } else {
            issuedBooks.joinToString("\n") { item ->
                "Book #${item.bookId} (${item.title}) -> ${item.studentId} [${item.status}]"
            }
        }
    }
}