package com.example.libraryassignmentkotlin

import java.util.Calendar

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val year: Int,
    var copies: Int
)

data class IssueRecord(
    val bookId: Int,
    val studentId: String,
    var returned: Boolean = false
)

data class IssuedBookView(
    val bookId: Int,
    val title: String,
    val studentId: String,
    val status: String
)

class LibraryService {
    private val books = mutableMapOf<Int, Book>()
    private val issues = mutableListOf<IssueRecord>()

    private fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    // Task 1: Add Books
    fun addBook(
        id: Int?,
        title: String,
        author: String,
        year: Int?,
        copies: Int?
    ): Pair<Boolean, String> {
        if (id == null || id <= 0) {
            return false to "Book ID must be a positive number."
        }
        if (books.containsKey(id)) {
            return false to "Book ID $id already exists."
        }

        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            return false to "Title cannot be blank."
        }

        val cleanAuthor = author.trim()
        if (cleanAuthor.isEmpty()) {
            return false to "Author cannot be blank."
        }

        val maxYear = currentYear()
        if (year == null || year < 1000 || year > maxYear) {
            return false to "Year must be between 1000 and $maxYear."
        }

        if (copies == null || copies < 0) {
            return false to "Copies must be a non-negative number."
        }

        books[id] = Book(id, cleanTitle, cleanAuthor, year, copies)
        return true to "'$cleanTitle' added successfully."
    }

    // Task 2: Display All Books
    fun getAllBooks(): List<Book> {
        return books.values.sortedBy { it.id }
    }

    // Task 3: Search for a Book (exact ID first, fallback to title substring)
    fun searchBooks(query: String): List<Book> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return emptyList()

        val id = cleanQuery.toIntOrNull()
        return if (id != null && books.containsKey(id)) {
            listOf(books.getValue(id))
        } else {
            books.values
                .filter { it.title.contains(cleanQuery, ignoreCase = true) }
                .sortedBy { it.id }
        }
    }

    // Task 4: Issue a Book
    fun issueBook(bookId: Int?, studentId: String): Pair<Boolean, String> {
        if (bookId == null || bookId <= 0) {
            return false to "Book ID must be a valid number."
        }
        val book = books[bookId] ?: return false to "No book found with ID $bookId."

        val cleanStudentId = studentId.trim()
        if (cleanStudentId.isEmpty()) {
            return false to "Student ID cannot be blank."
        }

        if (book.copies <= 0) {
            return false to "No copies of '${book.title}' are available."
        }

        val alreadyIssued = issues.any {
            it.bookId == bookId &&
                    it.studentId.equals(cleanStudentId, ignoreCase = true) &&
                    !it.returned
        }
        if (alreadyIssued) {
            return false to "Student $cleanStudentId already has this book issued."
        }

        book.copies--
        issues.add(IssueRecord(bookId, cleanStudentId))
        return true to "'${book.title}' issued to student $cleanStudentId."
    }

    // Task 5: Return a Book
    fun returnBook(bookId: Int?, studentId: String): Pair<Boolean, String> {
        if (bookId == null || bookId <= 0) {
            return false to "Book ID must be a valid number."
        }

        val cleanStudentId = studentId.trim()
        if (cleanStudentId.isEmpty()) {
            return false to "Student ID cannot be blank."
        }

        val record = issues.find {
            it.bookId == bookId &&
                    it.studentId.equals(cleanStudentId, ignoreCase = true) &&
                    !it.returned
        } ?: return false to "No outstanding issue record for book $bookId and student $cleanStudentId."

        record.returned = true
        books[bookId]?.let { it.copies++ }
        return true to "Book $bookId returned by student $cleanStudentId."
    }

    // Task 6: Display Currently Issued Books
    fun getIssuedBooks(): List<IssuedBookView> {
        return issues
            .filter { !it.returned }
            .map { record ->
                val title = books[record.bookId]?.title ?: "Unknown"
                IssuedBookView(
                    bookId = record.bookId,
                    title = title,
                    studentId = record.studentId,
                    status = "Issued"
                )
            }
    }
}