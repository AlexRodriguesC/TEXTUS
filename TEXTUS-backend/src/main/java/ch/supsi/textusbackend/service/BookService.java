package ch.supsi.textusbackend.service;

import ch.supsi.textusbackend.dataaccess.BookStorage;
import ch.supsi.textusbackend.model.Book;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookService implements IBookService {

    private static final Logger logger = LogManager.getLogger(BookService.class);
    private final BookStorage bookStorage;
    private final JobOperator jobOperator;
    private final Job pipelineJob;

    @Autowired
    public BookService(BookStorage bookStorage, JobOperator jobOperator, Job pipelineJob) {
        this.bookStorage = bookStorage;
        this.jobOperator = jobOperator;
        this.pipelineJob = pipelineJob;
    }

    @Override
    public void createBook(MultipartFile bookFile) {
        byte[] bookBytes = null;
        try {
            bookBytes = bookFile.getBytes();
        } catch (Exception e) {
            String message = String.format("Failed to read bytes for file %s", bookFile.getOriginalFilename());
            logger.error(message, e);
        }

        Book book = Book.create(bookFile.getOriginalFilename(), bookFile.getSize(), bookBytes);

        bookStorage.create(book);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("bookId", book.getBookId().toString())
                .addLong("triggeredAt", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobOperator.start(pipelineJob, jobParameters);
        } catch (Exception e) {
            String message = String.format("Failed to start job %s", pipelineJob.getName());
            logger.error(message, e);
        }
    }
}
