import java.io.*; // يقرا و يكتب داخل الملف
import java.nio.file.*; // حق الملفات
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator; // الي يرتب الملف
import java.util.Date;

public class LibraryBookTracker {

    public static void main(String[] args) {

        try {
            // خطأ اذا ما حط الارقمنت
            if (args.length < 2) {
                throw new InsufficientArgumentsException(
                        "You should write: java LibraryBookTracker catalog.txt <operation>");
            }

            String catalogName = args[0];
            String operation = args[1];

            // تأكد ان الملف txt
            if (!catalogName.endsWith(".txt")) {
                throw new InvalidFileNameException("Catalog file must be of the text type (Name.txt)");
            }

            Path catalogPath = Paths.get(catalogName);
            // اذا ما كان موجود انشئه
            if (!Files.exists(catalogPath)) {
                Files.createFile(catalogPath);
            }

            // احط الكتب الي في الملف في ليست من نوع book
            List<Book> books = new ArrayList<>();
            List<String> lines = Files.readAllLines(catalogPath);

            for (String line : lines) {
                Book book = parseBook(line);
                books.add(book);
            }
            // اذا كان ISBN ابحث في الليست عنه
            if (isISBN(operation)) {
                searchByISBN(books, operation);
            }
            // اذا كان كتاب جديد اضبفه
            else if (operation.contains(":")) {
                addNewBook(books, operation, catalogPath);
            }
            // اذا ولا واحد يعني ابحث بالعنوان
            else {
                searchByTitle(books, operation);
            }

        } catch (InsufficientArgumentsException e) {
            System.out.println(e.getMessage());
        } catch (InvalidFileNameException e) {
            System.out.println(e.getMessage());
        } catch (BookCatalogException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // ------------Methods---------

    // يحلل النص و يقسمه ويرجع اوبجكت Book
    private static Book parseBook(String line) throws MalformedBookEntryException {

        // مصفوفة لاقسام الكتاب
        String[] parts = line.split(":");

        // تأكد انها مقسمة اربقة اقسام بالزبط
        if (parts.length != 4) {
            throw new MalformedBookEntryException("Entry must contain 4 fields.");
        }

        String title = parts[0].trim();
        String author = parts[1].trim();
        String isbn = parts[2].trim();
        String copiesString = parts[3].trim();

        // تأكد من الشروط
        if (title.isEmpty() || author.isEmpty()) {
            throw new MalformedBookEntryException("Title and Author cannot be empty.");
        }
        if (!isISBN(isbn)) {
            throw new MalformedBookEntryException("ISBN must be exactly 13 digits.");
        }
        int copies;
        copies = Integer.parseInt(copiesString);
        if (copies <= 0) {
            throw new MalformedBookEntryException("Copies must be a positive integer.");
        }

        Book b = new Book(title, author, isbn, copies);
        return b;
    }

    // تبحث عن الكتب الي في عنوانها نفس الكلمة و تطبعها
    private static void searchByTitle(List<Book> books, String str) {
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);

            if (b.getTitle().contains(str)) {
                printBook(b);
            }
        }
    }

    // بحث عن الكتاب الي يطابق الرقم و يطبعه
    private static void searchByISBN(List<Book> books, String isbn) {

        List<Book> matches = new ArrayList<>();

        for (Book b : books) {
            if (b.getIsbn().equals(isbn)) {
                matches.add(b);
            }
        }

        try {
            if (matches.size() > 1) {
                throw new DuplicateISBNException(
                        "Multiple books found with ISBN: " + isbn);
            }
        } catch (DuplicateISBNException e) {
            System.out.println(e.getMessage());
        }

        if (matches.size() == 1) {
            printBook(matches.get(0));
        } else {
            System.out.println("No book found with ISBN: " + isbn);
        }
    }

    // اضافة كتاب جديد من عند المستخدم
    private static void addNewBook(List<Book> books, String record, Path catalogPath)
            throws MalformedBookEntryException {

        Book newBook = parseBook(record);
        books.add(newBook);

        books.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));

        try (BufferedWriter writer = Files.newBufferedWriter(catalogPath)) {
            for (Book b : books) {
                writer.write(b.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }

        printBook(newBook);
    }

    // تحقق اذا الرقم صحيح
    private static boolean isISBN(String input) {

        if (input.length() != 13) {
            return false;
        }

        for (int i = 0; i < input.length(); i++) {
            if (!Character.isDigit(input.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    // طباعة الكتب
    private static void printBook(Book b) {
        System.out.println("Title   ||   Author   ||   ISBN   ||   Copies");
        System.out.println(b.getTitle() + " || " + b.getAuthor() + " || " + b.getIsbn() + " || " + b.getCopies());
    }

}
