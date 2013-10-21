package com.atlassian.pocketknife.api.ao.dao;

import net.java.ao.schema.NotNull;

/**
 * Implemented by AOs that have a defined order
 */
public interface Positionable
{
    @NotNull
    public int getPos();
    public void setPos(int pos);
}
