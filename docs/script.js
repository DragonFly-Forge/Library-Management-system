// State arrays backed by localStorage
let books = JSON.parse(localStorage.getItem("lms_books")) || [];
let issues = JSON.parse(localStorage.getItem("lms_issues")) || [];

const CURRENT_YEAR = new Date().getFullYear();

// DOM Elements
const feedbackEl = document.getElementById("feedback-msg");
const addBookForm = document.getElementById("add-book-form");
const searchInput = document.getElementById("search-input");
const searchBtn = document.getElementById("search-btn");
const searchResultEl = document.getElementById("search-result");
const actionBookIdInput = document.getElementById("action-book-id");
const actionStudentIdInput = document.getElementById("action-student-id");
const issueBtn = document.getElementById("issue-btn");
const returnBtn = document.getElementById("return-btn");
const booksTbody = document.getElementById("books-tbody");
const issuesTbody = document.getElementById("issues-tbody");
const bookCountBadge = document.getElementById("book-count");
const issueCountBadge = document.getElementById("issue-count");

function saveState() {
  localStorage.setItem("lms_books", JSON.stringify(books));
  localStorage.setItem("lms_issues", JSON.stringify(issues));
}

function showFeedback(message, isError = false) {
  feedbackEl.textContent = message;
  feedbackEl.className = `feedback ${isError ? "error" : "success"}`;
}

// Escape HTML to prevent injection when rendering user-entered titles/authors
function escapeHtml(str) {
  const div = document.createElement("div");
  div.textContent = str;
  return div.innerHTML;
}

// Task 2: Render All Books Table
function renderBooks() {
  bookCountBadge.textContent = `${books.length} Book${books.length === 1 ? "" : "s"}`;

  if (books.length === 0) {
    booksTbody.innerHTML = `<tr><td colspan="5" class="muted">No books registered yet.</td></tr>`;
    return;
  }

  booksTbody.innerHTML = books
    .map(
      (book) => `
      <tr>
        <td>${book.id}</td>
        <td>${escapeHtml(book.title)}</td>
        <td>${escapeHtml(book.author)}</td>
        <td>${book.year}</td>
        <td>
          ${book.copies}
          <span class="status-pill ${book.copies > 0 ? "status-available" : "status-unavailable"}">
            ${book.copies > 0 ? "Available" : "Out of Stock"}
          </span>
        </td>
      </tr>
    `
    )
    .join("");
}

// Task 6: Render Currently Issued Books Table
function renderIssues() {
  const activeIssues = issues.filter((record) => !record.returned);
  issueCountBadge.textContent = `${activeIssues.length} Active`;

  if (activeIssues.length === 0) {
    issuesTbody.innerHTML = `<tr><td colspan="4" class="muted">No books are currently issued.</td></tr>`;
    return;
  }

  issuesTbody.innerHTML = activeIssues
    .map((record) => {
      const book = books.find((b) => b.id === record.bookId);
      const title = book ? escapeHtml(book.title) : "Unknown Title";
      return `
        <tr>
          <td>${record.bookId}</td>
          <td>${title}</td>
          <td>${escapeHtml(record.studentId)}</td>
          <td><span class="status-pill status-issued">Issued</span></td>
        </tr>
      `;
    })
    .join("");
}

// Task 1: Add Book
addBookForm.addEventListener("submit", (e) => {
  e.preventDefault();

  const id = Number.parseInt(document.getElementById("book-id").value, 10);
  const title = document.getElementById("book-title").value.trim();
  const author = document.getElementById("book-author").value.trim();
  const year = Number.parseInt(document.getElementById("book-year").value, 10);
  const copies = Number.parseInt(document.getElementById("book-copies").value, 10);

  if (!Number.isInteger(id) || id <= 0) {
    return showFeedback("Book ID must be a positive integer.", true);
  }
  if (books.some((b) => b.id === id)) {
    return showFeedback(`Book ID ${id} already exists. Duplicate IDs are not allowed.`, true);
  }
  if (!title || !author) {
    return showFeedback("Title and Author cannot be empty.", true);
  }
  if (!Number.isInteger(year) || year < 1000 || year > CURRENT_YEAR) {
    return showFeedback(`Publication year must be between 1000 and ${CURRENT_YEAR}.`, true);
  }
  if (!Number.isInteger(copies) || copies < 0) {
    return showFeedback("Available copies cannot be negative.", true);
  }

  books.push({ id, title, author, year, copies });
  saveState();
  renderBooks();
  addBookForm.reset();
  showFeedback(`Added "${title}" (ID: ${id}) successfully.`);
});

// Task 3: Search by ID or Title
function performSearch() {
  const query = searchInput.value.trim();
  if (!query) {
    searchResultEl.innerHTML = `<p class="muted">Enter a Book ID or title above to check details and availability.</p>`;
    return;
  }

  let matches = [];
  const numericId = Number(query);

  // Try exact ID match first if input is a valid integer
  if (Number.isInteger(numericId) && numericId > 0) {
    const byId = books.find((b) => b.id === numericId);
    if (byId) matches.push(byId);
  }

  // Fallback to case-insensitive title substring match
  if (matches.length === 0) {
    const lowerQuery = query.toLowerCase();
    matches = books.filter((b) => b.title.toLowerCase().includes(lowerQuery));
  }

  if (matches.length === 0) {
    searchResultEl.innerHTML = `<p class="muted">No books found matching "${escapeHtml(query)}".</p>`;
    return;
  }

  searchResultEl.innerHTML = matches
    .map(
      (b) => `
      <div class="search-item">
        <strong>#${b.id} — ${escapeHtml(b.title)}</strong> by ${escapeHtml(b.author)} (${b.year})<br />
        Copies: <strong>${b.copies}</strong> &nbsp;
        <span class="status-pill ${b.copies > 0 ? "status-available" : "status-unavailable"}">
          ${b.copies > 0 ? "Available" : "Out of Stock"}
        </span>
      </div>
    `
    )
    .join("");
}

searchBtn.addEventListener("click", performSearch);
searchInput.addEventListener("input", performSearch);

// Task 4: Issue Book
issueBtn.addEventListener("click", () => {
  const bookId = Number.parseInt(actionBookIdInput.value, 10);
  const studentId = actionStudentIdInput.value.trim();

  if (!Number.isInteger(bookId) || bookId <= 0 || !studentId) {
    return showFeedback("Enter a valid Book ID and Student ID to issue a book.", true);
  }

  const book = books.find((b) => b.id === bookId);
  if (!book) {
    return showFeedback(`Book ID ${bookId} does not exist.`, true);
  }
  if (book.copies <= 0) {
    return showFeedback(`No copies of "${book.title}" are currently available.`, true);
  }

  // Prevent issuing a duplicate copy of the same book to the same student
  const alreadyIssued = issues.some(
    (rec) => !rec.returned && rec.bookId === bookId && rec.studentId.toLowerCase() === studentId.toLowerCase()
  );
  if (alreadyIssued) {
    return showFeedback(`Student ${studentId} already holds an active copy of "${book.title}".`, true);
  }

  book.copies -= 1;
  issues.push({ bookId, studentId, returned: false });

  saveState();
  renderBooks();
  renderIssues();
  performSearch();
  showFeedback(`Issued "${book.title}" to Student ${studentId}.`);
});

// Task 5: Return Book
returnBtn.addEventListener("click", () => {
  const bookId = Number.parseInt(actionBookIdInput.value, 10);
  const studentId = actionStudentIdInput.value.trim();

  if (!Number.isInteger(bookId) || bookId <= 0 || !studentId) {
    return showFeedback("Enter a valid Book ID and Student ID to return a book.", true);
  }

  const activeRecord = issues.find(
    (rec) => !rec.returned && rec.bookId === bookId && rec.studentId.toLowerCase() === studentId.toLowerCase()
  );
  if (!activeRecord) {
    return showFeedback(`No active issue record found for Book ID ${bookId} and Student ${studentId}.`, true);
  }

  const book = books.find((b) => b.id === bookId);
  if (book) {
    book.copies += 1;
  }
  activeRecord.returned = true;

  saveState();
  renderBooks();
  renderIssues();
  performSearch();
  showFeedback(`Book ID ${bookId} returned by Student ${studentId}.`);
});

// Initial render on page load
renderBooks();
renderIssues();