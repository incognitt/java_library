import java.util.Scanner;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/lib";
        String user = "postgres";
        String password = "1234";

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Choose operation:");
            System.out.println("1. Add a book");
            System.out.println("2. Delete a book");
            System.out.println("3. Search books by keyword");
            System.out.println("4. Issue a book");
            System.out.println("5. Return a book");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            User userObj; // Объявляем переменную здесь, чтобы она была видна в блоке try

            switch (choice) {
                case 1:
                    System.out.println("Enter book title:");
                    String title = scanner.nextLine();
                    System.out.println("Enter author name:");
                    String author = scanner.nextLine();
                    System.out.println("Enter year:");
                    int year = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    System.out.println("Enter quantity:");
                    int quantity = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    Book bookToAdd = new Book(title, author, year, quantity, "", "");

                    bookToAdd.addBookToDatabase(url, user, password);
                    break;
                case 2:
                    System.out.println("Enter book title to delete:");
                    String titleToDelete = scanner.nextLine();
                    Book bookToDelete = new Book(titleToDelete);
                    bookToDelete.deleteBookFromDatabase(url, user, password);
                    break;
                case 3:
                    // Поиск книги
                    System.out.println("Choose search criteria:");
                    System.out.println("1. Title");
                    System.out.println("2. Author");
                    System.out.println("3. Year");
                    // Добавьте другие критерии поиска, если необходимо

                    int searchChoice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    String searchCriteria;
                    switch (searchChoice) {
                        case 1:
                            searchCriteria = "title";
                            break;
                        case 2:
                            searchCriteria = "author";
                            break;
                        case 3:
                            searchCriteria = "year";
                            break;
                        // Добавьте обработку других критериев поиска, если необходимо
                        default:
                            System.out.println("Invalid search criteria choice!");
                            return;
                    }

                    System.out.println("Enter keyword to search:");
                    String searchKeyword = scanner.nextLine();

                    Book book = new Book("", "", 0, 0, "", "");
                    List<Book> foundBooks = book.searchBookByKeyword(url, user, password, searchCriteria, searchKeyword);
                    for (Book foundBook : foundBooks) {
                        System.out.println(foundBook); // Вывод найденных книг
                    }
                    break;
                case 4:
                    // Issue a book
                    System.out.println("Enter user's last name:");
                    String userLastName = scanner.nextLine();
                    System.out.println("Enter user's first name:");
                    String userFirstName = scanner.nextLine();
                    System.out.println("Enter user's birth year:");
                    int userBirthYear = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    System.out.println("Enter the title of the book to issue:");
                    String issueTitle = scanner.nextLine();

                    userObj = new User(userLastName, userFirstName, userBirthYear);
                    boolean issued = userObj.issueBook(url, user, password, issueTitle, userFirstName, userLastName);

                    if (issued) {
                        System.out.println("Book '" + issueTitle + "' issued to " + userLastName + " " + userFirstName + ".");
                    } else {
                        System.out.println("Book '" + issueTitle + "' could not be issued.");
                    }
                    break;
                case 5:
                    // Return a book
                    System.out.println("Enter user's last name:");
                    userLastName = scanner.nextLine();
                    System.out.println("Enter user's first name:");
                    userFirstName = scanner.nextLine();

                    userObj = new User(userLastName, userFirstName, 0); // Birth year is not required for returning a book
                    List<Book> borrowedBooks = userObj.getBorrowedBooks(url, user, password);
                    if (!borrowedBooks.isEmpty()) {
                        System.out.println("Borrowed books for user " + userLastName + " " + userFirstName + ":");
                        for (Book borrowedBook : borrowedBooks) { // Rename 'book' to 'borrowedBook'
                            System.out.println(borrowedBook);
                        }
                        System.out.println("Enter the title of the book to return:");
                        String returnTitle = scanner.nextLine();

                        boolean returned = userObj.returnBook(url, user, password, returnTitle);
                        if (returned) {
                            System.out.println("Book '" + returnTitle + "' returned successfully.");
                        } else {
                            System.out.println("Book '" + returnTitle + "' could not be returned.");
                        }
                    } else {
                        System.out.println("No books borrowed by user " + userLastName + " " + userFirstName + ".");
                    }
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        } catch (Exception e) {
            System.err.println("Error occurred!");
            e.printStackTrace();
        }
    }
}
