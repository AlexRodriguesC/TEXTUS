package ch.supsi.textusbackend.service;

import ch.supsi.textusbackend.dataaccess.IDatabaseBookStorage;
import ch.supsi.textusbackend.dataaccess.IFileSystemBookStorage;
import ch.supsi.textusbackend.model.Book;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
public class BookService implements IBookService {

    private final IFileSystemBookStorage bookStorage;
    private final IDatabaseBookStorage databaseBookStorage;
    private final JobOperator jobOperator;
    private final Job pipelineJob;

    @Autowired
    public BookService(
            IFileSystemBookStorage bookStorage,
            IDatabaseBookStorage databaseBookStorage,
            JobOperator asyncJobOperator,
            Job pipelineJob) {
        this.bookStorage = bookStorage;
        this.databaseBookStorage = databaseBookStorage;
        this.jobOperator = asyncJobOperator;
        this.pipelineJob = pipelineJob;
    }

    @Override
    @Transactional // todo: this doesn't really work
    public void createBook(MultipartFile bookFile) {
        byte[] bookBytes = null;
        try {
            bookBytes = bookFile.getBytes();
        } catch (Exception e) {
            String message = String.format("Failed to read bytes for file %s", bookFile.getOriginalFilename());
            log.error(message, e);
        }

        Book book = Book.create(bookFile.getOriginalFilename(), bookFile.getSize(), bookBytes);

        Long bookId = databaseBookStorage.create(book);

        book = Book.create(bookId, book.getOriginalFilename(), book.getSize(), bookBytes);

        bookStorage.create(book);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("BOOK_ID", bookId)
                .addLong("TRIGGERED_AT", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobOperator.start(pipelineJob, jobParameters);
        } catch (Exception e) {
            String message = String.format("Failed to start job %s", pipelineJob.getName());
            log.error(message, e);
        }
    }
}
