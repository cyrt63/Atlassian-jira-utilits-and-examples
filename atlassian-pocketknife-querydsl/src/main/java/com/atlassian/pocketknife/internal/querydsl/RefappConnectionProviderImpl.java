package com.atlassian.pocketknife.internal.querydsl;

import com.atlassian.plugin.spring.scanner.annotation.component.RefappComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.RefappImport;
import com.atlassian.pocketknife.spi.querydsl.AbstractConnectionProvider;
import com.atlassian.refapp.api.ConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.SQLException;

@RefappComponent
public class RefappConnectionProviderImpl extends AbstractConnectionProvider
{

    private ConnectionProvider refappConnectionProvider;

    @Autowired
    public RefappConnectionProviderImpl(@RefappImport ConnectionProvider refappConnectionProvider)
    {
        this.refappConnectionProvider = refappConnectionProvider;
    }

    @Override
    protected Connection getConnectionImpl(boolean autoCommit)
    {
        try {
            return refappConnectionProvider.connection();
        }
        catch (SQLException e)
        {
            throw new RuntimeException();
        }
    }

}
