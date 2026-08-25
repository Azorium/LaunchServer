package ru.azorium.launchserver.exception;

public class ClientFolderAlreadyExistException extends RuntimeException{
    public ClientFolderAlreadyExistException(String message) {
        super(message);
    }
}
