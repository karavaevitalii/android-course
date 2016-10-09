package ru.ifmo.android_2016.calc;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Parser {

    private enum Token {
        ADD,
        SUB,
        MUL,
        DIV,
        NUM,
        END,
        ERR
    }

    private Token currentToken;
    private BigDecimal currentValue;
    private int pointer;
    private String expression;
    private static final String ERROR = "Bad Expression";

    private void nextToken() {
        if (pointer >= expression.length()) {
            currentToken = Token.END;
            return;
        }
        char token = expression.charAt(pointer++);
        switch (token) {
            case '+':
                currentToken = Token.ADD;
                break;
            case '-':
                currentToken = Token.SUB;
                break;
            case 'x':
                currentToken = Token.MUL;
                break;
            case '/':
                currentToken = Token.DIV;
                break;
            default:
                if (Character.isDigit(token) || token == '.') {
                    currentToken = Token.NUM;
                    StringBuilder number = new StringBuilder();
                    number.append(token);
                    while (pointer < expression.length() && (Character.isDigit(expression.charAt(pointer))
                            || expression.charAt(pointer) == '.')) {
                        number.append(expression.charAt(pointer));
                        ++pointer;
                    }
                    currentValue = new BigDecimal(number.toString());
                } else {
                    currentToken = Token.ERR;
                }
        }
    }

    public String parse(String expression) {
        this.expression = expression;
        this.pointer = 0;
        BigDecimal ret = expression();
        return ret.toString();
    }

    private BigDecimal expression() {
        BigDecimal left = term();
        while (currentToken == Token.ADD || currentToken == Token.SUB) {
            if (currentToken == Token.ADD) {
                left = left.add(term());
            }
            if (currentToken == Token.SUB) {
                left = left.subtract(term());
            }
        }
        return left;
    }

    private BigDecimal term() {
        BigDecimal left = value();
        while (currentToken == Token.MUL || currentToken == Token.DIV) {
            if (currentToken == Token.MUL) {
                left = left.multiply(value());
            }
            if (currentToken == Token.DIV) {
                left = left.divide(value(), 5, RoundingMode.HALF_EVEN);
            }
        }
        return left;
    }

    private BigDecimal value() {
        nextToken();
        switch (currentToken) {
            case NUM:
                BigDecimal number = currentValue;
                nextToken();
                return number;
            case SUB:
                return value().negate();
            default:
                throw new RuntimeException(ERROR);
        }
    }
}
