package gitlet;
import java.io.File;
import java.util.Date;
/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {
     /** Gitlet系统元数据存储目录 */
    public static final File GITLET_DIR = new File(".gitlet");
    /** 提交对象存储目录 */
    public static final File COMMITS_DIR = Utils.join(GITLET_DIR, "commits");
    /** 暂存区存储目录 */
    public static final File STAGING_DIR = Utils.join(GITLET_DIR, "staging");
    /** 当前提交的引用文件 */
    public static final File HEAD_FILE = Utils.join(GITLET_DIR, "HEAD");
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        // 参数为空
        if (args.length == 0) {
            System.out.println("请输入命令参数");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                if (args.length != 2) {
                    System.out.println("使用方法: java gitlet.Main add [文件名]");
                    System.exit(0);
                }
                add(args[1]);
                break;
            // TODO: FILL THE REST IN
            default:
                System.out.println("无效命令: " + firstArg);
                System.exit(0);
        }
    }
    /** 初始化Gitlet版本控制系统 */
    private static void init() {
        // 检查是否已存在Gitlet系统
        if (GITLET_DIR.exists()) {
            System.out.println("当前目录中已经存在一个Gitlet版本控制系统");
            System.exit(0);
        }

        // 创建Gitlet系统目录结构
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        STAGING_DIR.mkdir(); // 创建暂存区目录

        // 创建初始提交
        Commit initialCommit = new Commit();
        initialCommit.setMessage("初始提交");
        initialCommit.setTimestamp(new Date(0));  // Unix纪元时间

        // 保存初始提交
        initialCommit.save();

        // 设置HEAD指向初始提交
        String initialCommitHash = Utils.sha1(Utils.serialize(initialCommit));
        Utils.writeContents(HEAD_FILE, initialCommitHash);

        System.out.println("Gitlet版本控制系统初始化完成");
    }

    private static void add(String filename) {
        // 检查Gitlet是否已初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("尚未初始化Gitlet版本控制系统");
            System.exit(0);
        }

        File fileToAdd = new File(filename);

        // 检查文件是否存在
        if (!fileToAdd.exists()) {
            System.out.println("文件不存在。");
            System.exit(0);
        }

        // 读取当前HEAD指向的提交
        String headHash = Utils.readContentsAsString(HEAD_FILE);
        Commit headCommit = getCommit(headHash);

        // 读取文件内容
        byte[] fileContent = Utils.readContents(fileToAdd);
        String fileHash = Utils.sha1(fileContent);

        // 检查文件是否与当前提交中的版本相同
        if (headCommit.getTrackedFiles().containsKey(filename)) {
            String committedFileHash = headCommit.getTrackedFiles().get(filename);
            if (fileHash.equals(committedFileHash)) {
                // 如果相同，移除暂存区中的文件（如果存在）
                File stagedFile = Utils.join(STAGING_DIR, filename);
                if (stagedFile.exists()) {
                    stagedFile.delete();
                }
                return;
            }
        }

        // 将文件写入暂存区
        File stagedFile = Utils.join(STAGING_DIR, filename);
        Utils.writeContents(stagedFile, fileContent);
    }

    /** 添加getCommit方法在这里 */
    private static Commit getCommit(String commitHash) {
        File commitFile = Utils.join(COMMITS_DIR, commitHash);
        return Utils.readObject(commitFile, Commit.class);
    }
}
