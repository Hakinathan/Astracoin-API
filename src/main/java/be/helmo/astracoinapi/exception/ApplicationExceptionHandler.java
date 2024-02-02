package be.helmo.astracoinapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ResponseStatus(
            value = HttpStatus.NOT_FOUND,
            reason = "User not found"
    )
    @ExceptionHandler(UserNotFound.class)
    public void handleException(UserNotFound e){}

    @ResponseStatus(
            value = HttpStatus.FORBIDDEN,
            reason = "Password Not Matching"
    )
    @ExceptionHandler(PasswordNotMatching.class)
    public void handleException(PasswordNotMatching e){}

    @ResponseStatus(
            value = HttpStatus.FORBIDDEN,
            reason = "Email is already used"
    )
    @ExceptionHandler(EmailAlreadyUsed.class)
    public void handleException(EmailAlreadyUsed e){}

    @ResponseStatus(
            value = HttpStatus.FORBIDDEN,
            reason = "Email or Password is not matching"
    )
    @ExceptionHandler(EmailOrPasswordIsNotMatching.class)
    public void handleException(EmailOrPasswordIsNotMatching e){}

    @ResponseStatus(
            value = HttpStatus.NOT_FOUND,
            reason = "Currency destination not found"
    )
    @ExceptionHandler(CurrencyNotFound.class)
    public void handleException(CurrencyNotFound e){}

    @ResponseStatus(
            value = HttpStatus.NOT_ACCEPTABLE,
            reason = "Insufficient funds"
    )
    @ExceptionHandler(InsufficientFunds.class)
    public void handleException(InsufficientFunds e){}

    @ResponseStatus(
            value = HttpStatus.FORBIDDEN,
            reason = "Access forbiden"
    )
    @ExceptionHandler(ForbiddenException.class)
    public void handleException(ForbiddenException e){}

    @ResponseStatus(
            value = HttpStatus.INTERNAL_SERVER_ERROR,
            reason = "Internal Error"
    )
    @ExceptionHandler(InternalErrorException.class)
    public void handleException(InternalErrorException e){}

    @ResponseStatus(
            value = HttpStatus.BAD_REQUEST,
            reason = "FCM Token not acceptable"
    )
    @ExceptionHandler(FCMTokenException.class)
    public void handleException(FCMTokenException e){}


}
