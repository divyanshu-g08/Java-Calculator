# Precision Java Calculator

A robust, high-precision expression calculator built entirely in standard Java using the Swing GUI toolkit. This application is designed to be accessible for basic arithmetic while providing the ability to parse and evaluate complex mathematical strings with accurate operator precedence.

## Features

*   **Standard Operations:** Addition (+), Subtraction (-), Multiplication (*), Division (/).
*   **Extended Functions:** 
    *   Trigonometry: sin, cos, tan, asin, acos, atan
    *   Logarithms: base 10 (log), natural (ln)
    *   Exponents & Roots: Power (^), Square Root (sqrt)
    *   Factorials (!)
*   **Constants:** Pi (pi), Euler's Number (e).
*   **Expression Parsing:** Evaluates complex strings handling standard mathematical operator precedence (PEMDAS/BODMAS) and nested parentheses.
*   **High Precision Engine:** Core arithmetic uses `java.math.BigDecimal` with a configurable `MathContext` (currently set to 100 digits of precision) to eliminate common floating-point rounding errors.
*   **Cross-Platform UI:** Built with Java Swing, ensuring compatibility across Windows, macOS, and Linux environments without modification.
*   **Zero Dependencies:** Utilizes only standard Java libraries; no external mathematical or UI packages are required.

## Installation and Usage

### Prerequisites
*   Java Development Kit (JDK) 8 or higher installed on the host system.

### Execution Instructions

1.  **Save the Code:** Ensure the provided Java code is saved in a file exactly named `Calculator.java`.
2.  **Open Terminal/Command Prompt:** Navigate to the directory containing the file.
3.  **Compile:**
    ```bash
    javac Calculator.java
    ```
4.  **Execute:**
    ```bash
    java Calculator
    ```

## Architecture and Code Design

The codebase is contained within a single file to maximize portability and simplify deployment.

*   **UI Layer (`JFrame`, `JPanel`, `JButton`):** The graphical interface implements a standard `GridLayout` for the buttons and a `BorderLayout` for the overall frame. 
*   **Event Handling (`ActionListener`):** Button interactions append text to a `StringBuilder` buffer. The `=` operator triggers the evaluation engine.
*   **Evaluation Engine (Recursive Descent Parser):** The core evaluation logic relies on a `parseExpression` method. Instead of relying on external libraries, it utilizes a Recursive Descent Parser. This algorithm processes the mathematical string token by token, utilizing recursive method calls (`parseExpressionLevel1`, `parseTerm`, `parseFactor`) to strictly enforce operator precedence.

## Test Cases and Edge Cases

When modifying or evaluating the calculator, utilize the following test cases to verify accuracy:

### Standard Operations
*   **Basic Arithmetic:** `2 + 2 * 3` -> Expected: `8` (Verifies precedence)
*   **Parentheses:** `(2 + 2) * 3` -> Expected: `12`
*   **Negative Numbers:** `-5 + 10` -> Expected: `5`
*   **Nested Parentheses:** `2 * (3 + (4 / 2))` -> Expected: `10`

### Extended Operations
*   **Trigonometry:** `sin(pi/2)` -> Expected: `1`
*   **Exponents:** `2^10` -> Expected: `1024`
*   **Factorial:** `5!` -> Expected: `120`
*   **Combined:** `sqrt(16) + log(100)` -> Expected: `6`

### Handled Edge Cases
*   **Division by Zero:** `5 / 0` -> Outputs `Error` (Catches `ArithmeticException`).
*   **Syntax Errors:** `5 + * 2` or `(5+2` -> Outputs `Error` (Catches `RuntimeException`).
*   **Factorial of Non-Integers:** `2.5!` -> Outputs `Error` (Strict integer implementation).
*   **Factorial of Negative Numbers:** `-5!` -> Outputs `Error`.
*   **Chaining Calculations:** Inputting `5`, `+`, `5`, `=`, followed by `+`, `2`, `=` accurately evaluates to `12`.

## Limitations and Constraints

The requirement to maintain a single standard file introduces specific constraints compared to dedicated mathematical libraries:

1.  **Transcendental Function Precision:** Core arithmetic operations utilize arbitrary precision via `BigDecimal`. However, standard Java lacks native `BigDecimal` implementations for functions such as `sin()`, `cos()`, `log()`, and decimal `pow()`. Within the parser, these functions temporarily downcast to double-precision (`Math.sin()`, etc.) before returning to `BigDecimal`. This limits the precision of those specific operations to standard double limits (approximately 15-17 decimal places). 
2.  **Factorial Computation Limits:** While the underlying logic supports large integers, calculating massive factorials (e.g., `100000!`) on the Event Dispatch Thread (EDT) will cause UI blocking during computation.
3.  **UI Look and Feel:** The interface utilizes basic Swing components, defaulting to the system UI (`UIManager.getSystemLookAndFeelClassName()`). Appearance may vary slightly by operating system.
4.  **Implicit Multiplication:** The parser strictly requires explicit mathematical operators. Syntax such as `2(3+4)` is invalid and will return an error; it must be explicitly formatted as `2*(3+4)`.
5.  **Radian Mode Default:** Trigonometric functions (`sin`, `cos`, `tan`) inherently expect arguments in radians, matching standard Java `Math` behavior. Degree conversion is not natively toggled in the UI.

## Developer Notes

For developers reviewing or extending this codebase, note the following structural highlights:
1.  **UI Construction:** Observes standard Swing component assembly and layout management.
2.  **Event Dispatch:** Demonstrates thread-safe UI updates and action listener implementations.
3.  **The Parser (`parseExpression`):** Serves as a practical example of lexical analysis and parsing. It illustrates how compilers and interpreters process string-based instructions to enforce order of operations recursively.
