package me.aurous.utils.playlist;

import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JOptionPane;

import me.aurous.ui.UISession;
import me.aurous.ui.widgets.ExceptionWidget;
import me.aurous.ui.widgets.ImporterWidget;
import me.aurous.utils.Constants;
import me.aurous.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Andrew
 *
 */
public class YouTubePlayListImporter {

	public static void importYoutubePlayList(final String url,
			final String playListName) {
		final Thread thread = new Thread() {
			@Override
			public void run() {

				try {
					if (url.contains("playlist?")) {
						// fsyprint("Fetching %s...", url);
						String last = "";
						final String out = Constants.DATA_PATH + "playlist/"
								+ playListName + ".plist";
						final Document doc = Jsoup.connect(url).get();
						final Elements links = doc.select("a[href]");
						final File playListOut = new File(out);
						final FileOutputStream fos = new FileOutputStream(
								playListOut);
						final BufferedWriter bw = new BufferedWriter(
								new OutputStreamWriter(fos));
						final String header = "Title, Artist, Time, Date Added, User, Album, ALBUMART_INDEX, link";
						int iterations = 0;
						final ImporterWidget importWidget = UISession
								.getImporterWidget();
						bw.write(header);
						bw.newLine();
						for (final Element link : links) {
							if (((importWidget != null) && importWidget
									.isOpen())) {
								if ((importWidget != null)
										&& (importWidget.getImportProgressBar() != null)) {
									iterations += 1;

									final int percent = (int) ((iterations * 100.0f) / links
											.size());

									importWidget.getImportProgressBar()
											.setValue(percent);
									PlayListUtils.disableImporterInterface();
								}
								if (link.attr("abs:href").contains("watch?v=")
										&& (link.text().length() > 0)
										&& !link.text().contains("Play all")
										&& !link.text().contains("views")
										&& !link.attr("abs:href").equals(last)) {
									// System.out.println(formatYoutubeURL(link.attr("abs:href")));
									final String mediaLine = YouTubeDataFetcher
											.buildPlayListLine(link
													.attr("abs:href"));
									if (!mediaLine.isEmpty()) {
										bw.write(mediaLine);
										bw.newLine();
										last = link.attr("abs:href");

									}

								}
							} else {

								bw.close();
								PlayListUtils.deletePlayList(out);
								if (importWidget != null) {
									PlayListUtils.resetImporterInterface();
								}
								return;
							}
						}
						bw.close();
						if (importWidget != null) {
							PlayListUtils.resetImporterInterface();
						}

					} else {
						JOptionPane.showMessageDialog(null,
								"Invalid URL Detected must contain playlist?",
								"Error", JOptionPane.ERROR_MESSAGE);
						if (UISession.getImporterWidget() != null) {
							PlayListUtils.resetImporterInterface();
						}
					}
				} catch (HeadlessException | IOException e) {

					if (UISession.getImporterWidget() != null) {
						PlayListUtils.resetImporterInterface();
					}
					final ExceptionWidget eWidget = new ExceptionWidget(
							Utils.getStackTraceString(e, ""));
					eWidget.setVisible(true);
				}
			}
		};
		thread.start();

	}

}