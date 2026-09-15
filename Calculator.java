import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Precision Calculator
 * 
 * This calculator provides standard and extended mathematical operations 
 * with high precision using BigDecimal. It features a clean, user-friendly Swing GUI.
 * 
 * Key Features:
 * - High precision arithmetic using BigDecimal.
 * - Standard operations (+, -, *, /)
 * - Extended operations (sin, cos, tan, log, ln, sqrt, x^y)
 * - Expression evaluation handling operator precedence.
 * - Clean and modern-looking Swing interface.
 */
public class Calculator extends JFrame implements ActionListener {

    // UI Components
    private JTextField displayField;
    private JPanel buttonPanel;

    // Constants for precision
    private static final int PRECISION = 100; // High precision for core calculations
    private static final MathContext MC = new MathContext(PRECISION, RoundingMode.HALF_UP);

    // State variables
    private boolean isNewInput = true;
    private StringBuilder currentExpression = new StringBuilder();

    // Button layout definition
    private final String[] buttonLabels = {
            "7", "8", "9", "/", "C", "DEL",
            "4", "5", "6", "*", "sin", "asin",
            "1", "2", "3", "-", "cos", "acos",
            "0", ".", "=", "+", "tan", "atan",
            "(", ")", "^", "sqrt", "log", "ln",
            "pi", "e", "!"
    };

    /**
     * Constructor to initialize the calculator UI.
     */
    public Calculator() {
        // Window setup
        setTitle("Precision Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600); // Reasonably sized for most screens
        setLayout(new BorderLayout(10, 10)); // Add padding between components
        getContentPane().setBackground(new Color(240, 240, 245)); // Soft background

        // Display setup
        displayField = new JTextField();
        displayField.setEditable(false);
        displayField.setFont(new Font("Monospaced", Font.BOLD, 28));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setBackground(Color.WHITE);
        displayField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15) // Inner padding
        ));
        add(displayField, BorderLayout.NORTH);

        // Button panel setup
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 6, 8, 8)); // 6 rows, 6 cols, with gaps
        buttonPanel.setBackground(new Color(240, 240, 245));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create and add buttons
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            button.setFocusPainted(false);
            button.addActionListener(this);

            // Simple styling for different button types
            if (label.matches("[0-9.]")) {
                button.setBackground(Color.WHITE); // Numbers
            } else if (label.matches("[+\\-*/=]")) {
                button.setBackground(new Color(220, 230, 255)); // Basic operators
            } else if (label.equals("C") || label.equals("DEL")) {
                button.setBackground(new Color(255, 220, 220)); // Clear/Delete
            } else {
                button.setBackground(new Color(245, 245, 250)); // Extended functions
            }
            
            // Soft rounded look (basic approach in Swing)
            button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.CENTER);

        // Center on screen
        setLocationRelativeTo(null);
    }

    /**
     * Handles all button clicks.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("C")) {
            // Clear everything
            currentExpression.setLength(0);
            displayField.setText("");
            isNewInput = true;
        } else if (command.equals("DEL")) {
            // Delete last character
            if (currentExpression.length() > 0) {
                currentExpression.deleteCharAt(currentExpression.length() - 1);
                displayField.setText(currentExpression.toString());
            }
        } else if (command.equals("=")) {
            // Evaluate the expression
            try {
                String result = evaluate(currentExpression.toString());
                displayField.setText(result);
                currentExpression.setLength(0);
                currentExpression.append(result); // Allow chaining calculations
                isNewInput = true;
            } catch (Exception ex) {
                displayField.setText("Error");
                currentExpression.setLength(0);
                isNewInput = true;
            }
        } else {
            // Append the pressed button's label to the expression
            if (isNewInput && displayField.getText().equals("Error")) {
                currentExpression.setLength(0);
                displayField.setText("");
            }
            isNewInput = false;
            currentExpression.append(command);
            displayField.setText(currentExpression.toString());
        }
    }

    /**
     * Core evaluation engine using a Recursive Descent parser approach.
     * 
     * @param expression The mathematical string to evaluate.
     * @return The result as a String.
     */
    private String evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return "";
        }

        // Replace constants with their values before parsing
        expression = expression.replaceAll("pi", String.valueOf(Math.PI));
        expression = expression.replaceAll("e", String.valueOf(Math.E));

        return parseExpression(expression).stripTrailingZeros().toPlainString();
    }

    /**
     * A recursive descent parser to evaluate the math expression.
     * This allows handling operator precedence properly.
     */
    private BigDecimal parseExpression(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            BigDecimal parse() {
                nextChar();
                BigDecimal x = parseExpressionLevel1();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Addition and subtraction
            BigDecimal parseExpressionLevel1() {
                BigDecimal x = parseTerm();
                for (;;) {
                    if      (eat('+')) x = x.add(parseTerm(), MC);
                    else if (eat('-')) x = x.subtract(parseTerm(), MC);
                    else return x;
                }
            }

            // Multiplication and division
            BigDecimal parseTerm() {
                BigDecimal x = parseFactor();
                for (;;) {
                    if      (eat('*')) x = x.multiply(parseFactor(), MC);
                    else if (eat('/')) {
                        BigDecimal divisor = parseFactor();
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        x = x.divide(divisor, MC);
                    }
                    else return x;
                }
            }

            // Numbers, parentheses, functions, and exponents
            BigDecimal parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return parseFactor().negate(MC); // unary minus

                BigDecimal x;
                int startPos = this.pos;

                if (eat('(')) { // parentheses
                    x = parseExpressionLevel1();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = new BigDecimal(str.substring(startPos, this.pos), MC);
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor(); // argument for the function
                    
                    // Implement extended functions. 
                    // Note: We rely on java.lang.Math converted back to BigDecimal, 
                    // which limits trig/log to standard double precision (~15 digits).
                    double val = x.doubleValue();
                    switch (func) {
                        case "sqrt": x = new BigDecimal(Math.sqrt(val), MC); break;
                        case "sin":  x = new BigDecimal(Math.sin(val), MC); break;
                        case "cos":  x = new BigDecimal(Math.cos(val), MC); break;
                        case "tan":  x = new BigDecimal(Math.tan(val), MC); break;
                        case "asin": x = new BigDecimal(Math.asin(val), MC); break;
                        case "acos": x = new BigDecimal(Math.acos(val), MC); break;
                        case "atan": x = new BigDecimal(Math.atan(val), MC); break;
                        case "log":  x = new BigDecimal(Math.log10(val), MC); break;
                        case "ln":   x = new BigDecimal(Math.log(val), MC); break;
                        default: throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                // Handle exponents (^)
                if (eat('^')) {
                    BigDecimal exponent = parseFactor();
                    // We use double for pow here for simplicity.
                    x = new BigDecimal(Math.pow(x.doubleValue(), exponent.doubleValue()), MC);
                }
                
                // Handle factorial (!)
                if (eat('!')) {
                    x = factorial(x);
                }

                return x;
            }
            
            /**
             * Helper for calculating factorial of an integer represented as BigDecimal
             */
            BigDecimal factorial(BigDecimal n) {
                // Check if it's an integer
                if (n.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                     throw new ArithmeticException("Factorial only defined for integers");
                }
                
                int num = n.intValue();
                if (num < 0) throw new ArithmeticException("Factorial of negative number");
                
                BigDecimal fact = BigDecimal.ONE;
                for (int i = 2; i <= num; i++) {
                    fact = fact.multiply(new BigDecimal(i), MC);
                }
                return fact;
            }
        }.parse();
    }

    /**
     * Main method to launch the calculator.
     */
    public static void main(String[] args) {
        // Ensure UI is created on the Event Dispatch Thread for thread safety
        SwingUtilities.invokeLater(() -> {
            try {
                // Try to set system look and feel for native appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fallback to cross-platform look and feel
            }
            
            Calculator calc = new Calculator();
            calc.setVisible(true);
        });
    }
}
