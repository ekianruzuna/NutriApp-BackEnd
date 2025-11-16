package com.NutriApp.NutriApp.exceptions.Handlers;

import com.NutriApp.NutriApp.exceptions.*;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.SendFailedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolationException;
import org.eclipse.angus.mail.smtp.SMTPAddressFailedException;
import org.springframework.http.HttpHeaders;
import com.NutriApp.NutriApp.exceptions.*;
import com.NutriApp.NutriApp.modelo.Dia;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.core.SqlReturnType;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Podés agregar más manejadores para otras excepciones
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErrorGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error inesperado: " + ex.getMessage());
    }

    @ExceptionHandler(PersonaInvalidaException.class)
    public ResponseEntity<Map<String, String>> manejarPersonaInvalida(PersonaInvalidaException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DiaInvalidoException.class)
    public ResponseEntity<String> manejarPersonaInvalida(DiaInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UsuarioInvalidoException.class)
    public ResponseEntity<String> manejarUsuarioInvalido(UsuarioInvalidoException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(ComidaFavoritaException.class)
    public ResponseEntity<String> manejarComidaFavorita(ComidaFavoritaException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(UsuarioInexistenteException.class)
    public ResponseEntity<String> manejarUsuarioInexstente(UsuarioInexistenteException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }


    @ExceptionHandler(AuthorityInvalidaException.class)
    public ResponseEntity<String> manejarAuthorityInvalida(AuthorityInvalidaException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(ComidaIngeridaException.class)
    public ResponseEntity<String> manejarAuthorityInvalida(ComidaIngeridaException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(ActividadFisicaInvalidaException.class)
    public ResponseEntity<String> manejarActividadFisicaInvalida(ActividadFisicaInvalidaException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String campo = error.getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsuarioExistente.class)
    public ResponseEntity<Map<String, String>> manejarUsuarioExistente(UsuarioExistente ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler
    public ResponseEntity<String> manejarTipoNoSoportado(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("El contenido no es soportado = " + ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> manejarMailException(SMTPAddressFailedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El correo no se pudo mandar = " + ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> manejarAccessDeniedException(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage() + " = permisos insuficientes");
    }

    @ExceptionHandler
    public ResponseEntity<String> manejarNoResourceFoundException(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No existe la ruta especificada");
    }

    @ExceptionHandler
    public ResponseEntity<String> manejarMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Se requiere el parametro = " + ex.getParameterName());
    }

    @ExceptionHandler
    public ResponseEntity<String> manejarSolicitudInvalidaException(SolicitudInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> manejarAliemntoInvalidoException (AlimentoInvalidoException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> manejarConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errores = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            String mensaje = violation.getMessage();
            errores.put(path, mensaje);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException) {
            String targetType = invalidFormatException.getTargetType().getSimpleName();
            String invalidValue = invalidFormatException.getValue().toString();

            switch (targetType) {
                case "Genero":
                    return ResponseEntity.badRequest().body("Valor inválido para campo 'genero': '" + invalidValue + "'. Valores válidos: FEMENINO, MASCULINO.");
                case "NivelActividadFisica":
                    return ResponseEntity.badRequest().body("Valor inválido para campo 'nivelActividadFisica': '" + invalidValue + "'. Valores válidos: SEDENTARIO, LIGERA, INTENSA, MODERADA, MUY_INTENSA.");
                case "ObjetivoCaloricoTipo":
                    return ResponseEntity.badRequest().body("Valor inválido para campo 'objetivoCaloricoTipo': '" + invalidValue + "'. Valores válidos: DEFICIT_LIGERO, DEFICIT_MODERADO, MANTENIMIENTO, SUPERAVIT_LIGERO, SUPERAVIT_MODERADO.");
                case "TipoComida":
                    return ResponseEntity.badRequest().body("Valor inválido para campo 'tipoComida': '" + invalidValue + "'. Valores válidos: DESAYUNO, ALMUERZO, CENA, SNACK.");
                case "Role":
                    return ResponseEntity.badRequest().body("Valor inválido para campo 'role': '" + invalidValue + "'. Valores válidos: ADMIN, CLIENT.");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JSON inválido: " + ex.getMessage());
    }
}

