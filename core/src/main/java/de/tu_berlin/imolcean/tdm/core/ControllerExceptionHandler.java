package de.tu_berlin.imolcean.tdm.core;

import de.tu_berlin.imolcean.tdm.api.exceptions.DataSourceNotConfiguredException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler
{
    @ExceptionHandler({DataSourceNotConfiguredException.class})
    public final ResponseEntity<Object> handleEntityNotFound(Exception ex, WebRequest req)
    {
        return this.handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, req);
    }
}
