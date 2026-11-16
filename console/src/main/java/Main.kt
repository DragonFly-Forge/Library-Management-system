import java.time.Year

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

val books = mutableMapOf<Int, Book>()
val issues = mutableListOf<IssueRecord>()

// ---------- Input helpers ----------

fun readInt(prompt: String): Int? {
    print(prompt)
    return readlnOrNull()?.trim()?.toIntOrNull()
}

fun readText(prompt: String): String? {
    print(prompt)
    val text = readlnOrNull()?.trim() ?: ""
    return if (text.isBlank()) null else text
}

// ---------- Task 1: Add Books ----------

fun addBook() {
    val id = readInt("Book ID: ")
    if (id == null || id <= 0) {
        println("Error: ID must be a positive number.\n")
        return
    }
    if (books.containsKey(id)) {
        println("Error: Book ID already exists.\n")
        return
    }

    val title = readText("Title: ")
    if (title == null) {
        println("Error: Title cannot be blank.\n")
        return
    }

    val author = readText("Author: ")
    if (author == null) {
        println("Error: Author cannot be blank.\n")
        return
    }

    val year = readInt("Publication Year: ")
    if (year == null || year < 1000 || year > Year.now().value) {
        println("Error: Year must be a valid number between 1000 and ${Year.now().value}.\n")
        return
    }

    val copies = readInt("Copies available: ")
    if (copies == null || copies < 0) {
        println("Error: Copies must be a non-negative number.\n")
        return
    }

    books[id] = Book(id, title, author, year, copies)
    println("Success: '$title' added successfully!\n")
}

// ---------- Task 2: Display All Books ----------

fun displayBooks() {
    if (books.isEmpty()) {
        println("The library is currently empty.\n")
        return
    }
    val line = "-".repeat(74)
    println("\n" + "LIBRARY INVENTORY".padStart(45))
    println(line)
    println(String.format("%-5s | %-25s | %-20s | %-6s | %-6s", "ID", "Title", "Author", "Year", "Copies"))
    println(line)
    for (book in books.values.sortedBy { it.id }) {
        println(
            String.format(
                "%-5d | %-25s | %-20s | %-6d | %-6d",
                book.id, book.title.take(25), book.author.take(20), book.year, book.copies
            )
        )
    }
    println(line + "\n")
}

// ---------- Task 3: Search for a Book ----------

fun printBookDetails(book: Book) {
    val status = if (book.copies > 0) "Available (${book.copies} copies)" else "Out of stock"
    println("ID:      ${book.id}")
    println("Title:   ${book.title}")
    println("Author:  ${book.author}")
    println("Year:    ${book.year}")
    println("Status:  $status")
    println()
}

fun searchBook() {
    val query = readText("Enter book ID or title: ")
    if (query == null) {
        println("Error: Search cannot be blank.\n")
        return
    }

    // Try an ID match first; if the text isn't an ID (or no such ID), search titles.
    // This way a book titled "1984" can still be found.
    val id = query.toIntOrNull()
    val results = if (id != null && books.containsKey(id)) {
        listOf(books.getValue(id))
    } else {
        books.values.filter { it.title.contains(query, ignoreCase = true) }
    }

    if (results.isEmpty()) {
        println("No book found for '$query'.\n")
        return
    }
    println()
    results.forEach { printBookDetails(it) }
}

// ---------- Task 4: Issue a Book ----------

fun issueBook() {
    val bookId = readInt("Book ID: ")
    if (bookId == null) {
        println("Error: Book ID must be a number.\n")
        return
    }
    val book = books[bookId]
    if (book == null) {
        println("Error: No book with ID $bookId.\n")
        return
    }

    val studentId = readText("Student ID: ")
    if (studentId == null) {
        println("Error: Student ID cannot be blank.\n")
        return
    }

    if (book.copies <= 0) {
        println("Error: No copies of '${book.title}' are available.\n")
        return
    }
    if (issues.any { it.bookId == bookId && it.studentId == studentId && !it.returned }) {
        println("Error: Student $studentId already has this book issued.\n")
        return
    }

    book.copies--
    issues.add(IssueRecord(bookId, studentId))
    println("Success: '${book.title}' issued to student $studentId.\n")
}

// ---------- Task 5: Return a Book ----------

fun returnBook() {
    val bookId = readInt("Book ID: ")
    if (bookId == null) {
        println("Error: Book ID must be a number.\n")
        return
    }
    val studentId = readText("Student ID: ")
    if (studentId == null) {
        println("Error: Student ID cannot be blank.\n")
        return
    }

    val record = issues.find { it.bookId == bookId && it.studentId == studentId && !it.returned }
    if (record == null) {
        println("Error: No outstanding issue record for book $bookId and student $studentId.\n")
        return
    }

    record.returned = true
    books.getValue(bookId).copies++
    println("Success: Book $bookId returned by student $studentId.\n")
}

// ---------- Task 6: Display Issued Books ----------

fun displayIssued() {
    val active = issues.filter { !it.returned }
    if (active.isEmpty()) {
        println("No books are currently issued.\n")
        return
    }
    val line = "-".repeat(59)
    println("\n" + "CURRENTLY ISSUED BOOKS".padStart(40))
    println(line)
    println(String.format("%-5s | %-25s | %-12s | %-8s", "ID", "Title", "Student ID", "Status"))
    println(line)
    for (r in active) {
        val title = books[r.bookId]?.title ?: "Unknown"
        println(String.format("%-5d | %-25s | %-12s | %-8s", r.bookId, title.take(25), r.studentId.take(12), "Issued"))
    }
    println(line + "\n")
}

// ---------- Task 7: Menu ----------

fun printMenu() {
    println("===== Library Management System =====")
    println("1. Add a new book")
    println("2. Display all books")
    println("3. Search for a book")
    println("4. Issue a book")
    println("5. Return a book")
    println("6. Display currently issued books")
    println("7. Exit")
    print("Enter your choice: ")
}

fun main() {
    while (true) {
        printMenu()
        val choice = readlnOrNull()?.trim() ?: break
        println()
        when (choice) {
            "1" -> addBook()
            "2" -> displayBooks()
            "3" -> searchBook()
            "4" -> issueBook()
            "5" -> returnBook()
            "6" -> displayIssued()
            "7" -> {
                println("Goodbye!")
                break
            }
            else -> println("Invalid choice. Please enter a number from 1 to 7.\n")
        }
    }
}