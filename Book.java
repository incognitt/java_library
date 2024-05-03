import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class Book {
    private String title;
    private String author;
    private int year;
    private int quantity;
    private String borrowerFirstName;
    private String borrowerLastName;

    // Остальные поля и методы класса Book остаются без изменений

    public Book(String title) {
        this.title = title;
    }

    public Book(String title, String author, int year, int quantity, String borrowerFirstName, String borrowerLastName) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.quantity = quantity;
        this.borrowerFirstName = borrowerFirstName;
        this.borrowerLastName = borrowerLastName;
    }

    @Override
    public String toString() {
        return "Title: " + this.title +
                ", Author: " + this.author +
                ", Year: " + this.year +
                ", Quantity: " + this.quantity;
    }

    public void issueBook(String borrower) {
        if (this.quantity > 0) {
            this.quantity--;
            System.out.println("Book '" + this.title + "' is issued to " + borrower);
        } else {
            System.out.println("Sorry, book '" + this.title + "' is out of stock.");
        }
    }

    public void returnBook() {
        this.quantity++;
        System.out.println("Book '" + this.title + "' is returned to the library.");
    }

    // Остальные методы остаются без изменений

    public List<Book> searchBookByKeyword(String url, String user, String password, String searchCriteria, String keyword) {
        List<Book> foundBooks = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql;
            if (searchCriteria.equals("title") || searchCriteria.equals("author")) {
                sql = "SELECT * FROM books WHERE " + searchCriteria + " LIKE ?";
            } else if (searchCriteria.equals("year")) {
                sql = "SELECT * FROM books WHERE " + searchCriteria + " = ?";
            } else {
                System.out.println("Invalid search criteria!");
                return foundBooks;
            }

            PreparedStatement statement = connection.prepareStatement(sql);
            if (searchCriteria.equals("year")) {
                statement.setInt(1, Integer.parseInt(keyword)); // Преобразуем ключевое слово в целочисленный тип
            } else {
                statement.setString(1, "%" + keyword + "%");
            }
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                int year = resultSet.getInt("year");
                int quantity = resultSet.getInt("quantity");
                // Убрано получение borrower, так как этот столбец больше не используется
                Book book = new Book(title, author, year, quantity, borrowerFirstName, borrowerLastName);

                foundBooks.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Error searching for books by keyword!");
            e.printStackTrace();
        }
        return foundBooks;
    }


    public void deleteBookFromDatabase(String url, String user, String password) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "DELETE FROM books WHERE title = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, this.title);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Book '" + this.title + "' was deleted successfully!");
            } else {
                System.out.println("Book '" + this.title + "' was not found in the database!");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting data from the database!");
            e.printStackTrace();
        }
    }


    public void addBookToDatabase(String url, String user, String password) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Добавляем новую книгу в базу данных
            String sqlInsert = "INSERT INTO books (title, author, year, quantity) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(sqlInsert);
            insertStatement.setString(1, this.title);
            insertStatement.setString(2, this.author);
            insertStatement.setInt(3, this.year);
            insertStatement.setInt(4, this.quantity);
            int rowsInserted = insertStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new book was added successfully!");
            }

            // Уменьшаем количество книг в каталоге
            String sqlUpdate = "UPDATE books SET quantity = ? WHERE title = ?";
            PreparedStatement updateStatement = connection.prepareStatement(sqlUpdate);
            updateStatement.setInt(1, this.quantity);
            updateStatement.setString(2, this.title);
            int rowsUpdated = updateStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Book quantity updated successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error adding data to the database!");
            e.printStackTrace();
        }
    }



    public void updateBookQuantity(String url, String user, String password, String bookTitle, int quantity) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "UPDATE books SET quantity = ? WHERE title = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, quantity);
            statement.setString(2, bookTitle);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Book '" + bookTitle + "' quantity updated successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating book quantity!");
            e.printStackTrace();
        }
    }

    public void moveBookToBorrowed(String url, String user, String password, String borrowerFirstName, String borrowerLastName) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "INSERT INTO borrowed_books (book_title, author, year, quantity, borrower_first_name, borrower_last_name) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, this.title);
            statement.setString(2, this.author);
            statement.setInt(3, this.year);
            statement.setInt(4, this.quantity);
            statement.setString(5, borrowerFirstName);
            statement.setString(6, borrowerLastName);
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Book '" + this.title + "' was moved to borrowed books.");
                deleteBookFromDatabase(url, user, password); // Удаляем книгу из books
            }
        } catch (SQLException e) {
            System.err.println("Error moving book to borrowed books!");
            e.printStackTrace();
        }
    }




    public void moveBookFromBorrowed(String url, String user, String password) {
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String sql = "DELETE FROM borrowed_books WHERE title = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, this.title);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Book '" + this.title + "' was moved from borrowed books.");
                addBookToDatabase(url, user, password); // Добавляем книгу обратно в books
            }
        } catch (SQLException e) {
            System.err.println("Error moving book from borrowed books!");
            e.printStackTrace();
        }
    }

}



