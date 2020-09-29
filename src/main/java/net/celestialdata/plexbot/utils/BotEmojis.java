package net.celestialdata.plexbot.utils;

import com.vdurmont.emoji.EmojiParser;

/**
 * Provides an easy way to use emotes.
 *
 * @author Celestialdeath99
 */
public interface BotEmojis {
    /**
     * Returns the tools emote
     */
    String TOOLKIT = EmojiParser.parseToUnicode(":tools:");

    /**
     * Returns the tada emote
     */
    String SUCCESS = EmojiParser.parseToUnicode(":tada:");

    /**
     * Returns the white_square_button emote
     */
    String TODO_STEP = EmojiParser.parseToUnicode(":white_medium_square:");

    /**
     * Returns the white_check_mark emote
     */
    String FINISHED_STEP = EmojiParser.parseToUnicode(":white_check_mark:");

    /**
     * Returns the warning emote
     */
    String WARNING = EmojiParser.parseToUnicode(":warning:");

    /**
     * Returns the bangbang emote
     */
    String ERROR = EmojiParser.parseToUnicode(":bangbang:");

    /**
     * Returns the skull_crossbones emote
     */
    String SKULL = EmojiParser.parseToUnicode(":skull_crossbones:");

    /**
     * Returns the information_source emote
     */
    String INFO = EmojiParser.parseToUnicode(":information_source:");

    /**
     * Returns the grew_question emote
     */
    String QUESTION = EmojiParser.parseToUnicode(":grey_question:");

    /**
     * Returns the thumbsup emote
     */
    String THUMBS_UP = EmojiParser.parseToUnicode(":thumbsup:");

    /**
     * Returns the thumbsdown emote
     */
    String THUMBS_DOWN = EmojiParser.parseToUnicode(":thumbsdown:");

    /**
     * Returns the keyboard emote
     */
    String KEYBOARD = EmojiParser.parseToUnicode(":keyboard:");

    /**
     * Returns the notepad_spiral emote
     */
    String NOTEPAD = EmojiParser.parseToUnicode(":notepad_spiral:");

    /**
     * Returns the gear emote
     */
    String GEAR = EmojiParser.parseToUnicode(":gear:");

    /**
     * Returns the arrow_up emote
     */
    String UP_ARROW = EmojiParser.parseToUnicode(":arrow_up:");

    /**
     * Returns the arrow_down emote
     */
    String DOWN_ARROW = EmojiParser.parseToUnicode(":arrow_down:");

    String CHECK_MARK = EmojiParser.parseToUnicode(":white_check_mark:");

    String ARROW_RIGHT = EmojiParser.parseToUnicode(":arrow_right:");

    String ARROW_LEFT = EmojiParser.parseToUnicode(":arrow_left:");

    String X = EmojiParser.parseToUnicode(":x:");
}
