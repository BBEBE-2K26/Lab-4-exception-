import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MarksImporter {
    private static final Logger LOGGER =
            Logger.getLogger(MarksImporter.class.getName());

    // Custom unchecked exception for duplicate student IDs.
    static class DuplicateStudentException extends RuntimeException {
        public DuplicateStudentException(String message) {
            super(message);
        }
    }

    // Configure technical logging to a file, not the ordinary user's console.
    private static void configureLogger() {
        try {
            LOGGER.setUseParentHandlers(false);
            FileHandler handler = new FileHandler("marks-importer.log", true);
            handler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(handler);
        } catch (IOException e) {
            // Logging setup failure should not prevent the import from running.
            System.err.println("Warning: technical logging could not be enabled.");
        }
    }

    static void importMarks(String filename) throws IOException {
        Set<String> seen = new HashSet<>();
        int lineNumber = 0;

        // try-with-resources closes the reader automatically, even if an
        // exception occurs while processing or reading the file.
        try (BufferedReader br =
                     new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                try {
                    // TODO 1: Each record must contain exactly two fields.
                    String[] parts = line.split(",", -1);
                    if (parts.length != 2) {
                        throw new IllegalArgumentException(
                                "Each record must contain exactly two fields.");
                    }

                    String studentId = parts[0].trim();
                    String markText = parts[1].trim();

                    if (studentId.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Student ID cannot be empty.");
                    }

                    // TODO 2: Convert the mark and validate its range.
                    int mark = Integer.parseInt(markText);
                    if (mark < 0 || mark > 100) {
                        throw new IllegalArgumentException(
                                "Mark must be between 0 and 100.");
                    }

                    // TODO 3: HashSet.add returns false for an existing ID.
                    if (!seen.add(studentId)) {
                        throw new DuplicateStudentException(
                                "Duplicate student ID: " + studentId);
                    }

                    // TODO 4: Display the accepted record.
                    System.out.println(
                            "Accepted: " + studentId + " = " + mark);

                } catch (NumberFormatException e) {
                    System.out.println("Line " + lineNumber
                            + ": Invalid mark. Enter a whole number.");
                    LOGGER.log(Level.WARNING,
                            "NumberFormatException at line " + lineNumber, e);

                } catch (DuplicateStudentException e) {
                    System.out.println("Line " + lineNumber
                            + ": Duplicate student ID. Record skipped.");
                    LOGGER.log(Level.WARNING,
                            "Duplicate student record at line " + lineNumber, e);

                } catch (IllegalArgumentException e) {
                    System.out.println("Line " + lineNumber
                            + ": Invalid record - " + e.getMessage());
                    LOGGER.log(Level.WARNING,
                            "Invalid record at line " + lineNumber, e);
                }
            }
        }
    }

    public static void main(String[] args) {
        configureLogger();
        String filename = args.length > 0 ? args[0] : "marks.txt";

        try {
            importMarks(filename);
        } catch (FileNotFoundException e) {
            System.out.println(
                    "Marks file not found; check the file name.");
            LOGGER.log(Level.SEVERE,
                    "FileNotFoundException: " + e.getMessage(), e);
        } catch (IOException e) {
            System.out.println(
                    "An error occurred while reading the marks file.");
            LOGGER.log(Level.SEVERE,
                    "IOException: " + e.getMessage(), e);
        } finally {
            System.out.println("Import attempt finished");
        }
    }
}
