import java.io.File;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.Scanner;

public class FileCounter {
    static class StealingFileCountTask extends RecursiveTask<Integer> {
        private final File directory;
        private final String extension;

        public StealingFileCountTask(File directory, String extension) {
            this.directory = directory;
            this.extension = extension;
        }

        @Override
        protected Integer compute() {
            int count = 0;
            File[] files = directory.listFiles();

            if (files == null) {
                return 0;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    StealingFileCountTask subTask = new StealingFileCountTask(file, extension);
                    subTask.fork(); 
                    count += subTask.join(); 
                } else if (file.getName().endsWith(extension)) {
                    count++;
                }
            }

            return count;
        }
    }

    static class DealingFileCountTask extends RecursiveTask<Integer> {
        private final File directory;
        private final String extension;

        public DealingFileCountTask(File directory, String extension) {
            this.directory = directory;
            this.extension = extension;
        }

        @Override
        protected Integer compute() {
            int count = 0;
            File[] files = directory.listFiles();

            if (files == null) {
                return 0;
            }

            for (File file : files) {
                if (file.isDirectory()) {
                    DealingFileCountTask subTask = new DealingFileCountTask(file, extension);
                    subTask.fork(); 
                    count += subTask.join(); 
                } else if (file.getName().endsWith(extension)) {
                    count++;
                }
            }

            return count;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Input directory and file extension
        System.out.print("Enter the directory path: ");
        String directoryPath = scanner.nextLine();
        System.out.print("Enter the file extension (e.g., .pdf): ");
        String fileExtension = scanner.nextLine();

        File directory = new File(directoryPath);

        if (!directory.isDirectory()) {
            System.out.println("Invalid directory path.");
            return;
        }

        ForkJoinPool pool = new ForkJoinPool();

        long startTime = System.currentTimeMillis();
        StealingFileCountTask stealingTask = new StealingFileCountTask(directory, fileExtension);
        int stealingCount = pool.invoke(stealingTask);
        long endTime = System.currentTimeMillis();
        System.out.println("Work Stealing result: " + stealingCount);
        System.out.println("Execution time (Work Stealing): " + (endTime - startTime) + " ms");

        startTime = System.currentTimeMillis();
        DealingFileCountTask dealingTask = new DealingFileCountTask(directory, fileExtension);
        int dealingCount = pool.invoke(dealingTask);
        endTime = System.currentTimeMillis();
        System.out.println("Work Dealing result: " + dealingCount);
        System.out.println("Execution time (Work Dealing): " + (endTime - startTime) + " ms");
    }
}
