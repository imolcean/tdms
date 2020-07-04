package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.exceptions.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLException;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler
{
    @ExceptionHandler({
            StageDataSourceNotFoundException.class,
            TableNotFoundException.class,
            TableContentRowIndexOutOfBoundsException.class
    })
    public final ResponseEntity<Object> handleEntityNotFound(Exception ex, WebRequest req)
    {
        return this.handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, req);
    }

    @ExceptionHandler({
            StageDataSourceAlreadyExistsException.class
    })
    public final ResponseEntity<Object> handleEntityConflict(Exception ex, WebRequest req)
    {
        return this.handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT, req);
    }

    @ExceptionHandler({
            InvalidStageNameException.class,
            InvalidDataSourceAliasException.class,
            IllegalSizeOfTableContentRowException.class
    })
    public final ResponseEntity<Object> handleInvalidArgument(Exception ex, WebRequest req)
    {
        return this.handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, req);
    }

    @ExceptionHandler({
            NoCurrentStageException.class
    })
    public final ResponseEntity<Object> handleInvalidState(Exception ex, WebRequest req)
    {
        return this.handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.FAILED_DEPENDENCY, req);
    }

    @ExceptionHandler({
            SQLException.class
    })
    public final ResponseEntity<Object> handleSQLException(Exception ex, WebRequest req)
    {
        return this.handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
    }
}
