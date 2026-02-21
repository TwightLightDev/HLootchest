package org.twightlight.hlootchest.api.interfaces.lootchest;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.twightlight.hlootchest.api.enums.ButtonType;
import org.twightlight.hlootchest.api.interfaces.internal.TYamlWrapper;
import org.twightlight.hlootchest.objects.ButtonSound;

import java.util.List;

public interface TButton {
    /**
     * Gets the unique custom ID of the button.
     *
     * @return The custom ID of the button.
     */
    int getCustomId();

    /**
     * Gets the owner of the button.
     *
     * @return The {@link Player} who owns the button.
     */
    Player getOwner();

    /**
     * Removes the button.
     */
    void remove();

    /**
     * Equips an icon to the button.
     *
     * @param bukkiticon The {@link TIcon} to set as the button's icon.
     */
    void equipIcon(TIcon bukkiticon);

    /**
     * Moves the button forward in its container.
     */
    void moveForward();

    /**
     * Moves the button backward in its container.
     */
    void moveBackward();

    /**
     * Checks if the button has been moved.
     *
     * @return {@code true} if the button has moved, otherwise {@code false}.
     */
    boolean isMoved();

    /**
     * Checks if the button is clickable.
     *
     * @return {@code true} if the button is clickable, otherwise {@code false}.
     */
    boolean isClickable();

    /**
     * Sets whether the button is clickable.
     *
     * @param bool {@code true} to make the button clickable, {@code false} otherwise.
     */
    void setClickable(boolean bool);

    /**
     * Gets the type of the button.
     *
     * @return The {@link ButtonType} of the button.
     */
    ButtonType getType();

    /**
     * Gets the list of actions assigned to the button.
     *
     * @return A list of actions as string.
     */
    List<String> getActions();

    /**
     * Checks if the button is hidden.
     *
     * @return {@code true} if the button is hidden, otherwise {@code false}.
     */
    boolean isHiding();

    /**
     * Sets the icon of the button.
     *
     * @param icon The {@link TIcon} to set as the button's icon.
     */
    void setIcon(TIcon icon);

    /**
     * Sets the visibility of the button.
     *
     * @param isHiding {@code true} to hide the button, {@code false} to show it.
     */
    void hide(boolean isHiding);

    /**
     * Gets the sound associated with the button.
     *
     * @return The {@link ButtonSound} of the button.
     */
    ButtonSound getSound();

    /**
     * Gets the scheduled task associated with the button, if any.
     *
     * @return The {@link BukkitTask} assigned to the button.
     */
    @Deprecated
    BukkitTask getTask();

    /**
     * Checks if the button is movable.
     *
     * @return {@code true} if the button is movable, otherwise {@code false}.
     */
    boolean isMoveable();

    /**
     * Sets whether the button is movable.
     *
     * @param moveable {@code true} to make the button movable, {@code false} otherwise.
     */
    void setMoveable(boolean moveable);

    /**
     * Sets the type of the button.
     *
     * @param type The {@link ButtonType} to set for the button.
     */
    void setType(ButtonType type);

    /**
     * Sets the actions assigned to the button.
     *
     * @param actions A list of actions to assign to the button.
     */
    void setActions(List<String> actions);

    /**
     * Sets the sound associated with the button.
     *
     * @param sound The {@link ButtonSound} to set.
     */
    void setSound(ButtonSound sound);

    /**
     * Sets whether the button should be hidden.
     *
     * @param hiding {@code true} to hide the button, {@code false} to make it visible.
     */
    void setHidingState(boolean hiding);

    /**
     * Gets the current icon of the button.
     *
     * @return The {@link TIcon} representing the button's icon.
     */
    TIcon getIcon();

    /**
     * Sets the configuration manager for the button.
     *
     * @param config The {@link TYamlWrapper} to use for this button.
     */
    void setConfig(TYamlWrapper config);

    /**
     * Gets the path to the button in the configuration.
     *
     * @return The configuration path as a string.
     */
    String getPathToButton();

    /**
     * Sets the path to the button in the configuration.
     *
     * @param pathToButton The new configuration path.
     */
    void setPathToButton(String pathToButton);

    /**
     * Checks if the button has a dynamic name.
     *
     * @return {@code true} if the name is dynamic, otherwise {@code false}.
     */
    boolean isDynamicName();

    /**
     * Checks if the button has a dynamic icon.
     *
     * @return {@code true} if the icon is dynamic, otherwise {@code false}.
     */
    boolean isDynamicIcon();

    /**
     * Gets the name visibility mode of the button.
     *
     * @return The name visibility mode as a string.
     */
    String getNameVisibleMode();

    /**
     * Gets the configuration manager of the button.
     *
     * @return The {@link TYamlWrapper} associated with this button.
     */
    TYamlWrapper getConfig();

    boolean isPreview();
}
