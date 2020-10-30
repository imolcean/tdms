package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.exceptions.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// TODO Return a specific code on SQLException, not HTTP 500

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler
{
    @ExceptionHandler({
            StageDataSourceNotFoundException.class,
            TableNotFoundException.class,
            ColumnNotFoundException.class,
            TableContentRowIndexOutOfBoundsException.class,
            ImplementationNotFoundException.class
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
            IllegalSizeOfTableContentRowException.class,
            IllegalArgumentException.class
    })
    public final ResponseEntity<Object> handleIllegalArgument(Exception ex, WebRequest req)
    {
        return this.handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, req);
    }

    @ExceptionHandler({
            NoDataSourceSelectedException.class,
            NoCurrentStageException.class,
            NoImplementationSelectedException.class,
            NoOpenProjectException.class,
            NoGitRepositoryOpenException.class,
            GitRepositoryAlreadyOpenException.class,
            IllegalStateException.class,
            UnsupportedOperationException.class
    })
    public final ResponseEntity<Object> handleIllegalState(Exception ex, WebRequest req)
    {
        return this.handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.FAILED_DEPENDENCY, req);
    }

    @ExceptionHandler({
            Exception.class
    })
    public final ResponseEntity<Object> handleBrutalExceptions(Exception ex, WebRequest req)
    {
        return this.handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request)
    {
        ex.printStackTrace();

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }
}
