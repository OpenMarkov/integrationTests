package org.openmarkov.integrationTests.integrationTests.testOpenMarkovException2.exceptions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.OpenMarkovException;


@SuppressWarnings("ALL")
public class OpenFileException2 extends OpenMarkovException {
    
    public final @NotNull String fileName;
    public final @Nullable String owner;
    public final @Nullable String permissions;
    
    public OpenFileException2(String fileName, String owner, String permissions) {
        this.fileName = fileName;
        this.owner = owner;
        this.permissions = permissions;
    }
    
    @Override protected @Nullable String getExceptionTitle() {
        return "Cannot open file";
    }
    
    @Override protected @Nullable String getExceptionMessage() {
        return "Cannot open file: "+this.fileName;
    }
    
    
}

