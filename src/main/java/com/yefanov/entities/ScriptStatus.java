package com.yefanov.entities;

/**
 * Describes possible script statuses
 */
public enum ScriptStatus {

    /**
     * Script is still evaluating
     */
    RUNNING,

    /**
     * Script has been cancelled
     */
    CANCELLED,

    /**
     * Script has been completed exceptionally
     */
    COMPLETED_EXCEPTIONALLY,

    /**
     * Script is done
     */
    DONE;
}
