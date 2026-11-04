package com.martplus.desktop.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * UI Theme specifications for MARTplus brand identity.
 * Navy sidebar, indigo/purple primary accents, crisp typography, clean cards.
 */
public final class UITheme {

    // Color Palette
    public static final Color NAVY_PRIMARY = new Color(0x0F, 0x17, 0x2A);       // #0F172A Dark Slate/Navy
    public static final Color NAVY_SECONDARY = new Color(0x1E, 0x29, 0x3B);     // #1E293B Deep Navy Sidebar
    public static final Color NAVY_HOVER = new Color(0x33, 0x41, 0x55);         // #334155 Hover item
    public static final Color INDIGO_ACCENT = new Color(0x4F, 0x46, 0xE5);      // #4F46E5 Indigo
    public static final Color PURPLE_ACCENT = new Color(0x7C, 0x3A, 0xED);      // #7C3AED Purple
    public static final Color BLUE_ACCENT = new Color(0x25, 0x63, 0xEB);        // #2563EB Blue
    public static final Color BG_LIGHT = new Color(0xF8, 0xFA, 0xFC);           // #F8FAFC Off-white canvas
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_LIGHT = new Color(0xE2, 0xE8, 0xF0);       // #E2E8F0 Border
    public static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);       // #0F172A
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);         // #64748B
    public static final Color SUCCESS_GREEN = new Color(0x10, 0xB9, 0x81);      // #10B981
    public static final Color DANGER_RED = new Color(0xEF, 0x44, 0x44);         // #EF4444
    public static final Color WARNING_AMBER = new Color(0xF5, 0x9E, 0x0B);      // #F59E0B

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private UITheme() {
    }

    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        );
    }

    public static Border createPadding(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(INDIGO_ACCENT);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(INDIGO_ACCENT, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setForeground(NAVY_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(DANGER_RED);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(DANGER_RED, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(createCardBorder());
        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(BORDER_LIGHT);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(0xEE, 0xF2, 0xFF)); // Soft Indigo
        table.setSelectionForeground(NAVY_PRIMARY);
        table.getTableHeader().setFont(FONT_BODY_BOLD);
        table.getTableHeader().setBackground(new Color(0xF1, 0xF5, 0xF9));
        table.getTableHeader().setForeground(NAVY_PRIMARY);
        table.getTableHeader().setReorderingAllowed(false);
    }
}
