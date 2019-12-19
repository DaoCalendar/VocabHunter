/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.status;

import io.github.vocabhunter.analysis.session.FileNameTool;
import io.github.vocabhunter.gui.i18n.I18nKey;
import io.github.vocabhunter.gui.i18n.I18nManager;
import io.github.vocabhunter.gui.model.PositionModel;
import io.github.vocabhunter.gui.model.ProgressModel;
import io.github.vocabhunter.gui.model.StatusModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;

import static io.github.vocabhunter.gui.i18n.I18nKey.*;
import static javafx.beans.binding.Bindings.*;

@Singleton
public class StatusManagerImpl implements StatusManager {
    private static final Logger LOG = LoggerFactory.getLogger(StatusManagerImpl.class);

    private final I18nManager i18nManager;

    private final PositionDescriptionTool positionDescriptionTool;

    private I18nKey currentAction;

    private final SimpleBooleanProperty sessionAvailable = new SimpleBooleanProperty();

    private final SimpleBooleanProperty busy = new SimpleBooleanProperty();

    private final SimpleStringProperty positionDescription = new SimpleStringProperty("");

    private final SimpleStringProperty actionDescription = new SimpleStringProperty();

    private final SimpleDoubleProperty markedPercentage = new SimpleDoubleProperty();

    private final SimpleStringProperty graphText = new SimpleStringProperty();

    private final AtomicBoolean gatekeeper = new AtomicBoolean();

    @Inject
    public StatusManagerImpl(final I18nManager i18nManager, final PositionDescriptionTool positionDescriptionTool) {
        this.i18nManager = i18nManager;
        this.positionDescriptionTool = positionDescriptionTool;
    }

    @Inject
    public void setStatusModel(final StatusModel model) {
        model.textProperty().bind(when(busy).then(actionDescription).otherwise(positionDescription));
        model.busyProperty().bind(busy);
        model.activityProperty().bind(when(busy).then(-1).otherwise(0));
        model.graphShownProperty().bind(and(sessionAvailable, not(busy)));
        model.markedFractionProperty().bind(divide(markedPercentage, 100));
        model.graphTextProperty().bind(graphText);
    }

    @Override
    public boolean beginNewSession() {
        return begin(STATUS_ACTION_NEW);
    }

    @Override
    public boolean beginOpenSession() {
        return begin(STATUS_ACTION_OPEN);
    }

    @Override
    public boolean beginSaveSession() {
        return begin(STATUS_ACTION_SAVE);
    }

    @Override
    public boolean beginExport() {
        return begin(STATUS_ACTION_EXPORT);
    }

    @Override
    public boolean beginExit() {
        return begin(STATUS_ACTION_EXIT);
    }

    @Override
    public boolean beginAbout() {
        return begin(STATUS_ACTION_ABOUT);
    }

    private boolean begin(final I18nKey key) {
        if (gatekeeper.compareAndSet(false, true)) {
            currentAction = key;
            LOG.debug("Begin: {}", currentAction);
            actionDescription.setValue(i18nManager.text(key));
            busy.setValue(true);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void performAction(final Path file) {
        LOG.debug("Perform: {}", currentAction);
        actionDescription.setValue(String.format("%s: '%s'...", i18nManager.text(currentAction), FileNameTool.filename(file)));
    }

    @Override
    public void markSuccess() {
        LOG.debug("Success: {}", currentAction);
    }

    @Override
    public void completeAction() {
        LOG.debug("Complete: {}", currentAction);
        busy.setValue(false);
        gatekeeper.set(false);
    }

    @Override
    public void clearSession() {
        resetSession();
        positionDescription.set("");
        markedPercentage.set(0);
        graphText.set("");
        sessionAvailable.set(false);
    }

    @Override
    public void replaceSession(final PositionModel position, final ProgressModel progress) {
        resetSession();
        positionDescription.bind(positionDescriptionTool.createBinding(position, progress));
        markedPercentage.bind(progress.markedPercentVisibleProperty());
        graphText.bind(i18nManager.textBinding(STATUS_MARKED_PERCENTAGE, markedPercentage));
        sessionAvailable.set(true);
    }

    private void resetSession() {
        positionDescription.unbind();
        markedPercentage.unbind();
        graphText.unbind();
    }
}
