package results;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Results {
    private FileWriter fileWriter;
    private File file;
    private BufferedWriter writer;
    public Results(String fileName) throws IOException {
        this.file = new File(fileName);
        if (!file.exists()) {
            System.out.println(file.createNewFile());
        }
        this.fileWriter = new FileWriter(file);
        this.writer = new BufferedWriter(this.fileWriter);
    }

    public void writeResults(String results) throws IOException {
        this.writer.write(results);
    }

    public void close() throws IOException {
        this.writer.close();
    }
}
