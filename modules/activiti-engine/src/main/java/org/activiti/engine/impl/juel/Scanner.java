/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.activiti.engine.impl.juel;

import java.util.HashMap;



/**
 * Handcrafted scanner.
 *
 * @author Christoph Beck
 */
public class Scanner {
	/**
	 * Scan exception type
	 */
	@SuppressWarnings("serial")
	public static class ScanException extends Exception {
		final int position;
		final String encountered;
		final String expected;
		public ScanException(int position, String encountered, String expected) {
			super(LocalMessages.get("error.scan", position, encountered, expected));
			this.position = position;
			this.encountered = encountered;
			this.expected = expected;
		}
	}
	
	public static class Token {
		private final Symbol symbol;
		private final String image;
		private final int length;
		public Token(Symbol symbol, String image) {
			this(symbol, image, image.length());
		}
		public Token(Symbol symbol, String image, int length) {
			this.symbol = symbol;
			this.image = image;
			this.length = length;
		}
		public Symbol getSymbol() {
			return symbol;
		}
		public String getImage() {
			return image;
		}
		public int getSize() {
			return length;
		}
		@Override
		public String toString() {
			return symbol.toString();
		}
	}

	public static class ExtensionToken extends Token {
		public ExtensionToken(String image) {
			super(Scanner.Symbol.EXTENSION, image);
		}
	}
	
	/**
	 * Symbol type
	 */
	public enum Symbol {
		EOF,
		PLUS("'+'"), MINUS("'-'"),
		MUL("'*'"), DIV("'/'|'div'"), MOD("'%'|'mod'"),
		LPAREN("'('"), RPAREN("')'"),
		IDENTIFIER,
		NOT("'!'|'not'"), AND("'&&'|'and'"), OR("'||'|'or'"),
		EMPTY("'empty'"), INSTANCEOF("'instanceof'"),
		INTEGER, FLOAT, TRUE("'true'"), FALSE("'false'"), STRING, NULL("'null'"),
		LE("'<='|'le'"), LT("'<'|'lt'"), GE("'>='|'ge'"), GT("'>'|'gt'"),
		EQ("'=='|'eq'"), NE("'!='|'ne'"),
		QUESTION("'?'"), COLON("':'"),
		TEXT,
		DOT("'.'"), LBRACK("'['"), RBRACK("']'"),
		COMMA("','"),
		START_EVAL_DEFERRED("'#{'"), START_EVAL_DYNAMIC("'${'"), END_EVAL("'}'"),
		EXTENSION; // used in syntax extensions
		private final String string;
		private Symbol() {
			this(null);
		}
		private Symbol(String string) {
			this.string = string;
		}
		@Override
		public String toString() {
			return string == null ? "<" + name() + ">" : string;
		}
	}

	private static final HashMap<String, Token> KEYMAP = new HashMap<String, Token>();
	private static final HashMap<Symbol, Token> FIXMAP = new HashMap<Symbol, Token>();

	private static void addFixToken(Token token) {
		FIXMAP.put(token.getSymbol(), token);
	}

	private static void addKeyToken(Token token) {
		KEYMAP.put(token.getImage(), token);
	}	
	
	static {
		addFixToken(new Token(Symbol.PLUS, "+"));
		addFixToken(new Token(Symbol.MINUS, "-"));
		addFixToken(new Token(Symbol.MUL, "*"));
		addFixToken(new Token(Symbol.DIV, "/"));
		addFixToken(new Token(Symbol.MOD, "%"));
		addFixToken(new Token(Symbol.LPAREN, "("));
		addFixToken(new Token(Symbol.RPAREN, ")"));
		addFixToken(new Token(Symbol.NOT, "!"));
		addFixToken(new Token(Symbol.AND, "&&"));
		addFixToken(new Token(Symbol.OR, "||"));
		addFixToken(new Token(Symbol.EQ, "=="));
		addFixToken(new Token(Symbol.NE, "!="));
		addFixToken(new Token(Symbol.LT, "<"));
		addFixToken(new Token(Symbol.LE, "<="));
		addFixToken(new Token(Symbol.GT, ">"));
		addFixToken(new Token(Symbol.GE, ">="));
		addFixToken(new Token(Symbol.QUESTION, "?"));
		addFixToken(new Token(Symbol.COLON, ":"));
		addFixToken(new Token(Symbol.COMMA, ","));
		addFixToken(new Token(Symbol.DOT, "."));
		addFixToken(new Token(Symbol.LBRACK, "["));
		addFixToken(new Token(Symbol.RBRACK, "]"));
		addFixToken(new Token(Symbol.START_EVAL_DEFERRED, "#{"));
		addFixToken(new Token(Symbol.START_EVAL_DYNAMIC, "${"));
		addFixToken(new Token(Symbol.END_EVAL, "}"));
		addFixToken(new Token(Symbol.EOF, null, 0));
		
		addKeyToken(new Token(Symbol.NULL, "null"));
		addKeyToken(new Token(Symbol.TRUE, "true"));
		addKeyToken(new Token(Symbol.FALSE, "false"));
		addKeyToken(new Token(Symbol.EMPTY, "empty"));
		addKeyToken(new Token(Symbol.DIV, "div"));
		addKeyToken(new Token(Symbol.MOD, "mod"));
		addKeyToken(new Token(Symbol.NOT, "not"));
		addKeyToken(new Token(Symbol.AND, "and"));
		addKeyToken(new Token(Symbol.OR, "or"));
		addKeyToken(new Token(Symbol.LE, "le"));
		addKeyToken(new Token(Symbol.LT, "lt"));
		addKeyToken(new Token(Symbol.EQ, "eq"));
		addKeyToken(new Token(Symbol.NE, "ne"));
		addKeyToken(new Token(Symbol.GE, "ge"));
		addKeyToken(new Token(Symbol.GT, "gt"));
		addKeyToken(new Token(Symbol.INSTANCEOF, "instanceof"));
	}

	private Token token;  // current token
 	private int position; // start position of current token
	private final String input;
	
	protected final StringBuilder builder = new StringBuilder();
	
	/**
	 * Constructor.
	 * @param input expression string
	 */
	protected Scanner(String input) {
		this.input = input;
	}

	public String getInput() {
		return input;
	}
	
	/**
	 * @return current token
	 */
	public Token getToken() {
		return token;
	}

	/**
	 * @return current input position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @return <code>true</code> iff the specified character is a digit
	 */
	protected boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	/**
	 * @param s name
	 * @return token for the given keyword or <code>null</code>
	 */
	protected Token keyword(String s) {
		return KEYMAP.get(s);
	}
	
	/**
	 * @param symbol
	 * @return token for the given symbol
	 */
	protected Token fixed(Symbol symbol) {
		return FIXMAP.get(symbol);
	}

	protected Token token(Symbol symbol, String value, int length) {
		return new Token(symbol, value, length);
	}

	protected boolean isEval() {
		return token != null && token.getSymbol() != Symbol.TEXT && token.getSymbol() != Symbol.END_EVAL;
	}
	
	/**
	 * text token
	 */
	protected Token nextText() throws ScanException {
		builder.setLength(0);
		int i = position;
		int l = input.length();
		boolean escaped = false;
		while (i < l) {
			char c = input.charAt(i);
			switch (c) {
				case '\\':
					if (escaped) {
						builder.append('\\');
					} else {
						escaped = true;
					}
					break;
				case '#':
				case '$':
					if (i+1 < l && input.charAt(i+1) == '{') {
						if (escaped) {
							builder.append(c);
						} else {
							return token(Symbol.TEXT, builder.toString(), i - position);
						}
					} else {
						if (escaped) {
							builder.append('\\');
						}
						builder.append(c);
					}
					escaped = false;
					break;
				default:
					if (escaped) {
						builder.append('\\');
					}
					builder.append(c);
					escaped = false;
			}
			i++;
		}
		if (escaped) {
			builder.append('\\');
		}
		return token(Symbol.TEXT, builder.toString(), i - position);
	}
	
	/**
	 * string token
	 */
	protected Token nextString() throws ScanException {
		builder.setLength(0);
		char quote = input.charAt(position);
		int i = position+1;
		int l = input.length();
		while (i < l) {
			char c = input.charAt(i++);
			if (c == '\\') {
				if (i == l) {
					throw new ScanException(position, "unterminated string", quote + " or \\");
				} else {
					c = input.charAt(i++);
					if (c == '\\' || c == quote) {
						builder.append(c);
					} else {
						throw new ScanException(position, "invalid escape sequence \\" + c, "\\" + quote + " or \\\\");
					}
				}
			} else if (c == quote) {
				return token(Symbol.STRING, builder.toString(), i - position);
			} else {
				builder.append(c);
			}
		}
		throw new ScanException(position, "unterminated string", String.valueOf(quote));
	}
	
	/**
	 * number token
	 */
	protected Token nextNumber() throws ScanException {
		int i = position;
		int l = input.length();
		while (i < l && isDigit(input.charAt(i))) {
			i++;
		}
		Symbol symbol = Symbol.INTEGER;
		if (i < l && input.charAt(i) == '.') {
			i++;
			while (i < l && isDigit(input.charAt(i))) {
				i++;
			}
			symbol = Symbol.FLOAT;
		}
		if (i < l && (input.charAt(i) == 'e' || input.charAt(i) == 'E')) {
			int e = i;
			i++;
			if (i < l && (input.charAt(i) == '+' || input.charAt(i) == '-')) {
				i++;
			}
			if (i < l && isDigit(input.charAt(i))) {
				i++;
				while (i < l && isDigit(input.charAt(i))) {
					i++;
				}
				symbol = Symbol.FLOAT;
			} else {
				i = e;
			}
		}
		return token(symbol, input.substring(position, i), i - position);
	}
	
	/**
	 * token inside an eval expression
	 */
	protected Token nextEval() throws ScanException {
		char c1 = input.charAt(position);
		char c2 = position < input.length()-1 ? input.charAt(position+1) : (char)0;

		switch (c1) {
			case '*': return fixed(Symbol.MUL);
			case '/': return fixed(Symbol.DIV);
			case '%': return fixed(Symbol.MOD);
			case '+': return fixed(Symbol.PLUS);
			case '-': return fixed(Symbol.MINUS);
			case '?': return fixed(Symbol.QUESTION);
			case ':': return fixed(Symbol.COLON);
			case '[': return fixed(Symbol.LBRACK);
			case ']': return fixed(Symbol.RBRACK);
			case '(': return fixed(Symbol.LPAREN);
			case ')': return fixed(Symbol.RPAREN);
			case ',': return fixed(Symbol.COMMA);
			case '.':
				if (!isDigit(c2)) {
					return fixed(Symbol.DOT);
				}
				break;
			case '=':
				if (c2 == '=') {
					return fixed(Symbol.EQ);
				}
				break;
			case '&':
				if (c2 == '&') {
					return fixed(Symbol.AND);
				}
				break;
			case '|':
				if (c2 == '|') {
					return fixed(Symbol.OR);
				}
				break;
			case '!':
				if (c2 == '=') {
					return fixed(Symbol.NE);
				}
				return fixed(Symbol.NOT);
			case '<':
				if (c2 == '=') {
					return fixed(Symbol.LE);
				}
				return fixed(Symbol.LT);
			case '>': 
				if (c2 == '=') {
					return fixed(Symbol.GE);
				}
				return fixed(Symbol.GT);
			case '"':
			case '\'': return nextString();
		}
		
		if (isDigit(c1) || c1 == '.') {
			return nextNumber();
		}
		
		if (Character.isJavaIdentifierStart(c1)) {
			int i = position+1;
			int l = input.length();
			while (i < l && Character.isJavaIdentifierPart(input.charAt(i))) {
				i++;
			}
			String name = input.substring(position, i);
			Token keyword = keyword(name);
			return keyword == null ? token(Symbol.IDENTIFIER, name, i - position) : keyword;
		}

		throw new ScanException(position, "invalid character '" + c1 + "'", "expression token");
	}
	
	protected Token nextToken() throws ScanException {
		if (isEval()) {
			if (input.charAt(position) == '}') {
				return fixed(Symbol.END_EVAL);
			}
			return nextEval();
		} else {
			if (position+1 < input.length() && input.charAt(position+1) == '{') {
				switch (input.charAt(position)) {
					case '#':
						return fixed(Symbol.START_EVAL_DEFERRED);
					case '$':
						return fixed(Symbol.START_EVAL_DYNAMIC);
				}
			}
			return nextText();
		}
	}

	/**
	 * Scan next token.
	 * After calling this method, {@link #getToken()} and {@link #getPosition()}
	 * can be used to retreive the token's image and input position.
	 * @return scanned token
	 */
	public Token next() throws ScanException {
		if (token != null) {
			position += token.getSize();
		}
	
		int length = input.length();
				
		if (isEval()) {
			while (position < length && Character.isWhitespace(input.charAt(position))) {
				position++;
			}
		}

		if (position == length) {
			return token = fixed(Symbol.EOF);
		}

		return token = nextToken();
	}
}
