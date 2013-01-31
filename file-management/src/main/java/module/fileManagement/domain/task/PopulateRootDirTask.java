package module.fileManagement.domain.task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import module.fileManagement.domain.DirNode;
import module.fileManagement.domain.FileNode;
import module.fileManagement.domain.FileRepository;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import pt.ist.bennu.core.applicationTier.Authenticate;
import pt.ist.bennu.core.domain.User;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.vaadinframework.VaadinFrameworkLogger;

public class PopulateRootDirTask extends PopulateRootDirTask_Base {

	@Override
	public String getLocalizedName() {
		return getClass().getName();
	}

	@Override
	@Service
	public void executeTask() {
		doService();
	}

	public enum KeyEnum {
		TITLE("Título"), AUTHOR("Autor"), ENTITY("Entidade"), DEPARTMENT("Departamento"), DATE("Data"), DESCRIPTION("Descrição"),
		CATEGORY("Categoria"), ID("Identificador");

		private final String description;

		KeyEnum(String desc) {
			description = desc;
		}

		public String getDescription() {
			return description;
		}
	}

	private static final String PDF_DIR = "/tmp/populate/pdf/";
	private static final String WORDLIST_FILEPATH = "/tmp/populate/palavras.txt";
	private static final String PERSONS_FILEPATH = "/tmp/populate/pessoas.dat";
	private static final Random RANDOM = new Random();

	private Stack<File> files;
	private List names;
	private List words;
	private DateTime startDate;
	private DateTime endDate;
	private List<Integer> nfiles;
	private int nfilesindex;

	public PopulateRootDirTask() {
		super();
	}

	private void init() {
		final File dir = new File(PDF_DIR);
		files = new Stack();
		files.addAll(Arrays.asList(dir.listFiles()));
		names = getLines(PERSONS_FILEPATH, 100, "UTF-8");
		words = getLines(WORDLIST_FILEPATH, 100, "ISO-8859-1");
		startDate = new DateTime(1990, 01, 01, 0, 0, 0, 0);
		endDate = new DateTime();
		nfiles = getRandomFixSum(files.size(), 64);
		nfilesindex = 0;
	}

	private List<String> getLines(String fileName, int n, String encoding) {
		try {
			final List<String> readLines = FileUtils.readLines(new File(fileName), encoding);
			if (readLines.size() < n) {
				return readLines;
			}
			return readLines.subList(0, n);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}

	private String getRandom(List<String> strings) {
		int pos = RANDOM.nextInt(strings.size());
		return strings.get(pos);
	}

	private List<String> getRandom(List<String> strings, int count) {
		final Set<Integer> positions = new HashSet<Integer>();
		final List<String> result = new ArrayList<String>();
		while (positions.size() < count) {
			positions.add(RANDOM.nextInt(strings.size()));
		}
		for (Integer pos : positions) {
			result.add(strings.get(pos));
		}
		return result;
	}

	private String getDate() {
		final long millis = startDate.getMillis() + (long) (RANDOM.nextDouble() * (endDate.getMillis() - startDate.getMillis()));
		return new DateTime(millis).toString("dd-MM-yyyy");
	}

	private String getValue(KeyEnum keyEnum) {
		switch (keyEnum) {
		case TITLE:
			return org.apache.commons.lang.StringUtils.join(getRandom(words, RANDOM.nextInt(10)), " ");
		case AUTHOR:
			return getRandom(names);
		case ID:
			return Integer.toString(RANDOM.nextInt(500));
		case DATE:
			return getDate();
		default:
			return getRandom(words);
		}
	}

	private Map<String, String> getMetadataMap() {
		final HashMap<String, String> metadata = new HashMap<String, String>();
		for (KeyEnum keyEnum : KeyEnum.values()) {
			metadata.put(keyEnum.getDescription(), getValue(keyEnum));
		}
		return metadata;
	}

	private void createFile(DirNode node, File file) {
		final FileNode fileNode = node.createFile(file, file.getName(), file.length(), node.getContextPath());
		fileNode.getDocument().addMetadata(getMetadataMap());
	}

	private void fillDir(DirNode node) {
		if (nfilesindex > nfiles.size() - 1) {
			return;
		}
		int n = nfiles.get(nfilesindex);
		nfilesindex++;
		VaadinFrameworkLogger.getLogger().debug(String.format("fill %s with %d files\n", node.getName(), n));
		while (n-- > 0 && !files.isEmpty()) {
			createFile(node, files.pop());
		}
	}

	private static List<Integer> getRandomFixSum(int total, int n) {

		TreeSet<Integer> points = new TreeSet<Integer>();

		for (int i = 0; i < n - 1; i++) {
			points.add(RANDOM.nextInt(total - 1) + 1);
		}

		final List<Integer> first = new ArrayList<Integer>();
		first.add(0);
		first.addAll(points);

		final List<Integer> end = new ArrayList<Integer>();
		end.addAll(points);
		end.add(total);

		assert first.size() == end.size() && end.size() == n;

		List<Integer> result = new ArrayList<Integer>();

		for (int i = 0; i < first.size(); i++) {
			result.add(end.get(i) - first.get(i));
		}

		int sum = 0;

		for (int i : result) {
			sum += i;
		}

		assert sum == total;

		return result;
	}

	protected void doService() {
		init();
		final User user = User.findByUsername("ist24439");
		final DirNode rootDir = FileRepository.getOrCreateFileRepository(user);
		DirNode node1, node2;
		Authenticate.authenticate(user);
		final int MAX = 4;
		for (int i = 1; i <= MAX; i++) {
			String name = Integer.toString(i);
			node1 = rootDir.createDir(name, rootDir.getContextPath());
			fillDir(node1);
			for (int j = 1; j <= MAX; j++) {
				node2 = node1.createDir(name + "_" + j, node1.getContextPath());
				fillDir(node2);
				for (int k = 1; k <= MAX; k++) {
					fillDir(node2.createDir(name + "_" + j + "_" + k, node2.getContextPath()));
				}
			}
		}
		pt.ist.fenixWebFramework.security.UserView.setUser(null);
	}

}
