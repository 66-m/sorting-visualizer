package io.github.compilerstuck.Control.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Styled card component with rounded corners, shadow effect, and consistent padding.
 * Hover effects for modern feel with smooth transitions.
 */
public class StyledCard extends JPanel {
    private boolean isHovered = false;
    
    private int cornerRadius = UiTheme.BORDER_RADIUS;
    private int padding = UiTheme.SPACING_MD;
    private Color borderColor = UiTheme.BORDER_COLOR;
    
    public StyledCard() {
        this(new BorderLayout());
    }
    
    public StyledCard(LayoutManager layout) {
        super(layout);
        initialize();
    }
    
    private void initialize() {
        setBackground(UiTheme.BG_SECONDARY); // white
        setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        setOpaque(false); // we paint our own background        
        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        int shadowOffset = isHovered ? 3 : 2;  // lift on hover
        int shadowAlpha1 = isHovered ? 18 : 10; // deeper shadow on hover
        int shadowAlpha2 = isHovered ? 12 : 6;
        
        // Layered soft shadow (more prominent on hover)
        g2.setColor(new Color(0, 0, 0, shadowAlpha1));
        g2.fillRoundRect(0, shadowOffset, w, h, cornerRadius, cornerRadius);
        g2.setColor(new Color(0, 0, 0, shadowAlpha2));
        g2.fillRoundRect(0, shadowOffset + 1, w, h, cornerRadius, cornerRadius);
        
        // White card background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);
        
        // Border color shifts subtly on hover
        Color border = isHovered ? new Color(219, 219, 227) : borderColor; // slightly darker on hover
        g2.setColor(border);
        g2.setStroke(new BasicStroke(UiTheme.BORDER_WIDTH));
        g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }
}
