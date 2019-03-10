package arsatech.co.justifiedtextview;

import android.graphics.Paint;
import android.view.Gravity;
import android.widget.TextView;

class Utils {

	public static void justify(TextView textView) {
		Paint paint = new Paint();

		String[] blocks;
		float spaceOffset;
		float textWrapWidth;

		int spacesToSpread;
		float wrappedEdgeSpace;
		String block;
		String[] lineAsWords;
		String wrappedLine;
		StringBuilder smb = new StringBuilder();
		Object[] wrappedObj;

		// Pull widget properties
		paint.setColor(textView.getCurrentTextColor());
		paint.setTypeface(textView.getTypeface());
		paint.setTextSize(textView.getTextSize());

		textWrapWidth = textView.getWidth();
		spaceOffset = paint.measureText(" ");
		blocks = textView.getText().toString().split("((?<=\n)|(?=\n))");

		if (textWrapWidth < 20) {
			return;
		}

		for (int i = 0; i < blocks.length; i++) {
			block = blocks[i];

			if (block.length() == 0) {
				continue;
			} else if (block.equals("\n")) {
				smb.append(block);
				continue;
			}

			block = block.trim();

			if (block.length() == 0) continue;

			wrappedObj = Utils.createWrappedLine(block, paint, spaceOffset, textWrapWidth);
			wrappedLine = ((String) wrappedObj[0]);
			wrappedEdgeSpace = (Float) wrappedObj[1];
			lineAsWords = wrappedLine.split(" ");
			spacesToSpread = (int) (wrappedEdgeSpace != Float.MIN_VALUE ? wrappedEdgeSpace / spaceOffset : 0);

			for (String word : lineAsWords) {
				smb.append(word).append(" ");

				if (--spacesToSpread > 0) {
					smb.append(" ");
				}
			}

			smb = new StringBuilder(smb.toString().trim());

			if (blocks[i].length() > 0) {
				blocks[i] = blocks[i].substring(wrappedLine.length());

				if (blocks[i].length() > 0) {
					smb.append("\n");
				}

				i--;
			}
		}

		textView.setGravity(Gravity.LEFT);
		textView.setText(smb.toString());
	}

	public static Object[] createWrappedLine(String block, Paint paint, float spaceOffset, float maxWidth) {
		float cacheWidth;
		float origMaxWidth = maxWidth;

		StringBuilder line = new StringBuilder();

		for (String word : block.split("\\s")) {
			cacheWidth = paint.measureText(word);
			maxWidth -= cacheWidth;

			if (maxWidth <= 0) {
				return new Object[]{line.toString(), maxWidth + cacheWidth + spaceOffset};
			}

			line.append(word).append(" ");
			maxWidth -= spaceOffset;

		}

		if (paint.measureText(block) <= origMaxWidth) {
			return new Object[]{block, Float.MIN_VALUE};
		}
		return new Object[]{line.toString(), maxWidth};
	}

	private final static String SYSTEM_NEWLINE = "\n";
	private final static float COMPLEXITY = 5.12f;  //Reducing this will increase efficiency but will decrease effectiveness
	private final static Paint p = new Paint();

	public static void justify(final TextView tv, float origWidth) {
		String s = tv.getText().toString();
		p.setTypeface(tv.getTypeface());
		String[] splits = s.split(SYSTEM_NEWLINE);
		float width = origWidth - 5;
		for (int x = 0; x < splits.length; x++)
			if (p.measureText(splits[x]) > width) {
				splits[x] = wrap(splits[x], width);
				String[] microSplits = splits[x].split(SYSTEM_NEWLINE);
				for (int y = 0; y < microSplits.length - 1; y++)
					microSplits[y] = justify(removeLastSpace(microSplits[y]), width);
				StringBuilder smb_internal = new StringBuilder();
				for (int z = 0; z < microSplits.length; z++)
					smb_internal.append(microSplits[z]).append((z + 1 < microSplits.length) ? SYSTEM_NEWLINE : "");
				splits[x] = smb_internal.toString();
			}
		final StringBuilder smb = new StringBuilder();
		for (String cleaned : splits)
			smb.append(cleaned).append(SYSTEM_NEWLINE);
		tv.setGravity(Gravity.LEFT);
		tv.setText(smb);
	}

	private static String wrap(String s, float width) {
		String[] str = s.split("\\s"); //regex
		StringBuilder smb = new StringBuilder(); //save memory
		smb.append(SYSTEM_NEWLINE);
		for (String aStr : str) {
			float length = Utils.p.measureText(aStr);
			String[] pieces = smb.toString().split(SYSTEM_NEWLINE);
			try {
				if (Utils.p.measureText(pieces[pieces.length - 1]) + length > width)
					smb.append(SYSTEM_NEWLINE);
			} catch (Exception ignored) {
			}
			smb.append(aStr).append(" ");
		}
		return smb.toString().replaceFirst(SYSTEM_NEWLINE, "");
	}

	private static String removeLastSpace(String s) {
		if (s.contains(" ")) {
			int index = s.lastIndexOf(" ");
			int indexEnd = index + 1;
			if (index == 0)
				return s.substring(1);
			else if (index == s.length() - 1)
				return s.substring(0, index);
			else
				return s.substring(0, index) + s.substring(indexEnd);
		}
		return s;
	}

	private static String justifyOperation(String s, float width) {
		float holder = (float) (COMPLEXITY * Math.random());
		while (s.contains(Float.toString(holder)))
			holder = (float) (COMPLEXITY * Math.random());
		String holder_string = Float.toString(holder);
		float lessThan = width;
		int timeOut = 100;
		int current = 0;
		while (Utils.p.measureText(s) < lessThan && current < timeOut) {
			s = s.replaceFirst(" ([^" + holder_string + "])", " " + holder_string + "$1");
			lessThan = Utils.p.measureText(holder_string) + lessThan - Utils.p.measureText(" ");
			current++;
		}
		return s.replaceAll(holder_string, " ");
	}

	private static String justify(String s, float width) {
		while (Utils.p.measureText(s) < width) {
			s = justifyOperation(s, width);
		}
		return s;
	}

}
