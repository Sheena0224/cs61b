package gitlet;
import java.io.File;
import java.util.Date;
import java.io.IOException;
import java.util.HashMap;
import java.util.Formatter;
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
            System.exit(0);//终止程序运行
        }
        String firstArg = args[0];//获取命令行第一个参数
        switch(firstArg) {
            case "init"://初始化
                // TODO: handle the `init` command
                init();
                break;
            case "add"://添加
                // TODO: handle the `add [filename]` command
                if (args.length != 2) {
                    System.out.println("使用方法: java gitlet.Main add [文件名]");
                    System.exit(0);
                }
                add(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit"://提交
                if (args.length != 2) {
                    System.out.println("使用方法: java gitlet.Main commit [消息]");
                    System.exit(0);
                }
                commit(args[1]);
                break;
            case "rm"://删除
                if (args.length != 2) {
                    System.out.println("使用方法: java gitlet.Main rm [文件名]");
                    System.exit(0);
                }
                rm(args[1]);
                break;
            case "log"://日志
                if (args.length != 1) {
                    System.out.println("使用方法: java gitlet.Main log");
                    System.exit(0);
                }
                log();
                break;
            case "find"://查找
                if (args.length != 2) {
                    System.out.println("使用方法: java gitlet.Main find [提交信息]");
                    System.exit(0);
                }
                find(args[1]);
                break;
            case "status":
                if (args.length != 1) {
                    System.out.println("使用方法: java gitlet.Main status");
                    System.exit(0);
                }
                status();
                break;
            case "checkout":
                if (args.length < 2) {
                    System.out.println("使用方法: java gitlet.Main checkout [选项] [参数]");
                    System.exit(0);
                }

                if (args[1].equals("--") && args.length == 3) {
                    // 用法1: checkout -- [文件名]
                    checkoutFileFromHead(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    // 用法2: checkout [提交ID] -- [文件名]
                    checkoutFileFromCommit(args[1], args[3]);
                } else if (args.length == 2) {
                    // 用法3: checkout [分支名称]
                    checkoutBranch(args[1]);
                } else {
                    System.out.println("无效的checkout命令格式");
                    System.exit(0);
                }
                break;
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
        Commit initialCommit = new Commit();//创建空提交对象
        initialCommit.setMessage("初始提交");//设置提交信息
        initialCommit.setTimestamp(new Date(0));  // Unix纪元时间

        // 保存初始提交
        initialCommit.save();//保存到.gitlet/commits/目录

        // 设置HEAD指向初始提交
        String initialCommitHash = Utils.sha1(Utils.serialize(initialCommit));
        Utils.writeContents(HEAD_FILE, initialCommitHash);

        System.out.println("Gitlet版本控制系统初始化完成");
        File refsDir = Utils.join(GITLET_DIR, "refs");
        File headsDir = Utils.join(refsDir, "heads");
        refsDir.mkdir();
        headsDir.mkdir();

        // 创建主分支引用文件
        File masterBranch = Utils.join(headsDir, "主分支");
        Utils.writeContents(masterBranch, initialCommitHash);

        // 设置HEAD指向主分支
        Utils.writeContents(HEAD_FILE, "ref: refs/heads/主分支");
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
        Commit headCommit = getCommitFromRef(headHash);

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
    private static Commit getCommitFromRef(String reference) {
        // 处理分支引用
        if (reference.startsWith("ref:")) {
            String branchPath = reference.substring(4).trim();
            File branchFile = Utils.join(GITLET_DIR, branchPath);
            if (branchFile.exists()) {
                String commitHash = Utils.readContentsAsString(branchFile);
                return getCommit(commitHash); // 获取实际提交
            }
        }

        // 直接提交哈希
        return getCommit(reference);
    }

    /** 添加getCommit方法在这里 */
    private static Commit getCommit(String reference) {
        // 如果是分支引用（以 "ref:" 开头）
        if (reference.startsWith("ref:")) {
            String branchPath = reference.substring(4).trim(); // 移除 "ref:" 前缀
            File branchFile = Utils.join(GITLET_DIR, branchPath);
            if (branchFile.exists()) {
                String commitHash = Utils.readContentsAsString(branchFile);
                return getCommit(commitHash); // 递归获取实际提交
            }
        }
        File commitFile = Utils.join(COMMITS_DIR, reference);
        return Utils.readObject(commitFile, Commit.class);
    }

    /** 提交命令实现 */
    private static void commit(String message) {
        // 检查Gitlet是否已初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("尚未初始化Gitlet版本控制系统");
            System.exit(0);
        }

        // 检查提交消息是否为空
        if (message == null || message.trim().isEmpty()) {
            System.out.println("请输入一个提交信息。");
            System.exit(0);
        }

        // 检查暂存区是否为空
        String[] stagedFiles = STAGING_DIR.list();
        if (stagedFiles == null || stagedFiles.length == 0) {
            System.out.println("未向提交中添加任何变更。");
            System.exit(0);
        }

        // 读取当前HEAD指向的提交
        String headHash = Utils.readContentsAsString(HEAD_FILE);
        Commit parentCommit = getCommit(headHash);

        // 创建新提交
        Commit newCommit = new Commit();
        newCommit.setMessage(message);
        newCommit.setTimestamp(new Date());
        newCommit.setParent(headHash); // 设置父提交

        // 继承父提交的文件跟踪状态
        HashMap<String, String> newTrackedFiles = new HashMap<>(parentCommit.getTrackedFiles());
        File[] rmMarkers = STAGING_DIR.listFiles((dir, name) -> name.startsWith("RM_"));
        if (rmMarkers != null) {
            for (File rmMarker : rmMarkers) {
                String filename = rmMarker.getName().substring(3); // 移除"RM_"前缀
                newTrackedFiles.remove(filename); // 从跟踪文件中移除
                rmMarker.delete(); // 删除标记文件
            }
        }

        // 处理暂存区中的文件
        for (String filename : stagedFiles) {
            File stagedFile = Utils.join(STAGING_DIR, filename);
            if (stagedFile.exists()) {
                // 读取文件内容并计算哈希
                byte[] fileContent = Utils.readContents(stagedFile);
                String fileHash = Utils.sha1(fileContent);

                //将文件内容保存到 commits 目录
                File contentFile = Utils.join(COMMITS_DIR, fileHash);
                Utils.writeContents(contentFile, fileContent);

                // 更新或添加文件到跟踪列表
                newTrackedFiles.put(filename, fileHash);

                // 删除暂存文件
                stagedFile.delete();
            }
        }

        // 设置新提交的跟踪文件
        newCommit.setTrackedFiles(newTrackedFiles);

        // 保存新提交
        newCommit.save();

        // 更新HEAD指向新提交
        String newCommitHash = Utils.sha1(Utils.serialize(newCommit));
        Utils.writeContents(HEAD_FILE, newCommitHash);

        System.out.println("提交完成: " + message);
    }

    /** 移除文件 */
    private static void rm(String filename) {
        // 检查Gitlet是否已初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("尚未初始化Gitlet版本控制系统");
            System.exit(0);
        }

        // 读取当前HEAD指向的提交
        String headHash = Utils.readContentsAsString(HEAD_FILE);
        Commit headCommit = getCommit(headHash);

        // 检查文件是否被跟踪
        boolean isTracked = headCommit.getTrackedFiles().containsKey(filename);

        // 检查文件是否在暂存区
        File stagedFile = Utils.join(STAGING_DIR, filename);
        boolean isStaged = stagedFile.exists();

        if (!isTracked && !isStaged) {
            System.out.println("没有理由删除该文件。");
            System.exit(0);
        }

        // 如果文件被跟踪，从暂存区标记为删除（创建空文件作为标记）
        if (isTracked) {
            File rmMarker = Utils.join(STAGING_DIR, "RM_" + filename);
            try {
                rmMarker.createNewFile();
            } catch (IOException e) {
                System.out.println("无法创建删除标记文件");
                System.exit(0);
            }
        }

        // 如果文件在暂存区，移除它
        if (isStaged) {
            stagedFile.delete();
        }
    }

    /** 日志命令实现 - 显示提交历史 */
    private static void log() {
        // 检查Gitlet是否已初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("尚未初始化Gitlet版本控制系统");
            System.exit(0);
        }

        // 获取当前HEAD指向的提交哈希
        String currentHash = Utils.readContentsAsString(HEAD_FILE);

        // 遍历提交历史
        while (currentHash != null) {
            Commit commit = getCommit(currentHash);

            // 按照图片要求的格式显示提交信息
            System.out.println("===");
            System.out.println("commit " + currentHash);

            // 格式化日期（按照图片要求显示本地时区时间）
            java.util.Formatter formatter = new java.util.Formatter();
            formatter.format("日期: %ta %tb %td %tT %tY %tz",
                    commit.getTimestamp(), commit.getTimestamp(), commit.getTimestamp(),
                    commit.getTimestamp(), commit.getTimestamp(), commit.getTimestamp());
            System.out.println(formatter.toString());
            formatter.close();

            System.out.println(commit.getMessage());
            System.out.println();

            // 移动到父提交
            currentHash = commit.getParent();
        }
    }

    /** 查找命令实现 - 根据提交消息查找提交 */
    private static void find(String commitMessage) {
        // 检查Gitlet是否已初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("尚未初始化Gitlet版本控制系统");
            System.exit(0);
        }

        boolean found = false;

        // 遍历所有提交文件
        String[] commitFiles = COMMITS_DIR.list();
        if (commitFiles != null) {
            for (String commitHash : commitFiles) {
                try {
                    Commit commit = getCommit(commitHash);
                    if (commit.getMessage().equals(commitMessage)) {
                        System.out.println(commitHash);
                        found = true;
                    }
                } catch (Exception e) {
                    // 忽略无效的提交文件
                    continue;
                }
            }
        }

        // 如果没有找到匹配的提交
        if (!found) {
            System.out.println("找不到具有该消息的提交");
            System.exit(0);
        }
    }

    /** 状态命令实现 - 显示完整状态信息 */
    private static void status() {
        if (!GITLET_DIR.exists()) {
            System.out.println("尚未初始化Gitlet版本控制系统");
            System.exit(0);
        }

        // 1. 显示分支信息
        System.out.println("=== 分支 ===");
        System.out.println("*主分支");
        System.out.println();

        // 2. 显示暂存文件（已添加）
        System.out.println("=== 阶段化文件 ===");
        displayStagedFiles();
        System.out.println();

        // 3. 显示删除文件（已标记删除）
        System.out.println("=== 已删除文件 ===");
        displayRemovedFiles();
        System.out.println();
    }

    /** 显示暂存文件 */
    private static void displayStagedFiles() {
        String[] stagedFiles = STAGING_DIR.list();
        if (stagedFiles != null) {
            for (String filename : stagedFiles) {
                if (!filename.startsWith("RM_")) {
                    System.out.println(filename);
                }
            }
        }
    }

    /** 显示删除文件 */
    private static void displayRemovedFiles() {
        String[] stagedFiles = STAGING_DIR.list();
        if (stagedFiles != null) {
            for (String filename : stagedFiles) {
                if (filename.startsWith("RM_")) {
                    System.out.println(filename.substring(3));
                }
            }
        }
    }
    /** checkout 用法1: 从HEAD提交获取文件 */
    private static void checkoutFileFromHead(String filename) {
        // 1. 获取HEAD指向的提交
        String headHash = Utils.readContentsAsString(HEAD_FILE);
        Commit commit = getCommit(headHash);

        // 2. 检查文件是否被跟踪
        if (!commit.getTrackedFiles().containsKey(filename)) {
            System.out.println("文件内容不存在");
            return;
        }

        // 3. 获取存储的文件内容哈希
        String fileHash = commit.getTrackedFiles().get(filename);
        File sourceFile = Utils.join(COMMITS_DIR, fileHash);

        // 4. 恢复文件内容
        if (sourceFile.exists()) {
            byte[] content = Utils.readContents(sourceFile);
            Utils.writeContents(new File(filename), content);
        } else {
            System.out.println("文件内容不存在");
        }
    }

    /** checkout 用法2: 从指定提交获取文件 */
    private static void checkoutFileFromCommit(String commitHash, String filename) {
        // 检查Gitlet是否已初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("尚未初始化Gitlet版本控制系统");
            System.exit(0);
        }

        // 解析可能缩写的提交哈希
        String fullHash = resolveCommitHash(commitHash);
        if (fullHash == null) {
            System.out.println("不存在具有该标识的提交");
            System.exit(0);
        }

        // 获取指定提交
        Commit commit = getCommit(fullHash);

        // 检查文件是否存在于此提交中
        if (!commit.getTrackedFiles().containsKey(filename)) {
            System.out.println("该提交中不存在该文件。");
            System.exit(0);
        }

        // 获取文件内容并写入工作目录
        String fileHash = commit.getTrackedFiles().get(filename);
        File sourceFile = Utils.join(COMMITS_DIR, fileHash); // 假设文件内容存储在commits目录
        File destFile = new File(filename);

        if (sourceFile.exists()) {
            byte[] content = Utils.readContents(sourceFile);
            Utils.writeContents(destFile, content);
        } else {
            System.out.println("文件内容不存在");
            System.exit(0);
        }
    }

    /** checkout 用法3: 切换到指定分支 */
    private static void checkoutBranch(String branchName) {
        // 检查Gitlet是否已初始化
        if (!GITLET_DIR.exists()) {
            System.out.println("尚未初始化Gitlet版本控制系统");
            System.exit(0);
        }

        // 检查分支是否存在（需要实现分支功能）
        File branchFile = Utils.join(GITLET_DIR, "refs", "heads", branchName);
        if (!branchFile.exists()) {
            System.out.println("不存在该分支");
            System.exit(0);
        }

        // 检查是否为当前分支
        String currentBranch = getCurrentBranch();
        if (branchName.equals(currentBranch)) {
            System.out.println("无需切换至当前分支");
            System.exit(0);
        }

        checkUntrackedFilesConflict(branchName);

        String branchHeadHash = Utils.readContentsAsString(branchFile);
        Commit branchHeadCommit = getCommit(branchHeadHash);

        clearWorkingDirectory();
        restoreFilesFromCommit(branchHeadCommit);

        Utils.writeContents(HEAD_FILE, "ref: refs/heads/" + branchName);

        clearStagingArea();
    }

    /** 解析可能缩写的提交哈希 */
    private static String resolveCommitHash(String shortHash) {
        if (shortHash.length() == 40) {
            File commitFile = Utils.join(COMMITS_DIR, shortHash);
            return commitFile.exists() ? shortHash : null;
        }

        // 缩写哈希，查找匹配的提交
        String[] commitFiles = COMMITS_DIR.list();
        if (commitFiles != null) {
            for (String fullHash : commitFiles) {
                if (fullHash.startsWith(shortHash)) {
                    return fullHash;
                }
            }
        }
        return null;
    }

    /** 检查未跟踪文件冲突 */
    private static void checkUntrackedFilesConflict(String targetBranch) {
        // 获取当前HEAD提交
        String currentHeadHash = Utils.readContentsAsString(HEAD_FILE);
        Commit currentCommit = getCommit(currentHeadHash);

        File branchFile = Utils.join(GITLET_DIR, "refs", "heads", targetBranch);
        String branchHeadHash = Utils.readContentsAsString(branchFile);
        Commit branchCommit = getCommit(branchHeadHash);

        // 检查工作目录中的未跟踪文件
        File[] workingFiles = new File(".").listFiles();
        if (workingFiles != null) {
            for (File file : workingFiles) {
                if (file.isFile() && !file.getName().equals(".gitlet")) {
                    String filename = file.getName();

                    // 如果文件未被当前提交跟踪，但会被目标分支覆盖
                    if (!currentCommit.getTrackedFiles().containsKey(filename) &&
                            branchCommit.getTrackedFiles().containsKey(filename)) {
                        System.out.println("当前路径中存在未跟踪文件；请先删除该文件，然后添加并提交");
                        System.exit(0);
                    }
                }
            }
        }
    }

    /** 从提交恢复文件到工作目录 */
    private static void restoreFilesFromCommit(Commit commit) {
        for (String filename : commit.getTrackedFiles().keySet()) {
            String fileHash = commit.getTrackedFiles().get(filename);
            File sourceFile = Utils.join(COMMITS_DIR, fileHash);
            File destFile = new File(filename);

            if (sourceFile.exists()) {
                byte[] content = Utils.readContents(sourceFile);
                Utils.writeContents(destFile, content);
            }
        }
    }

    /** 清空工作目录 */
    private static void clearWorkingDirectory() {
        File[] files = new File(".").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !file.getName().equals(".gitlet")) {
                    file.delete();
                }
            }
        }
    }

    /** 清空暂存区 */
    private static void clearStagingArea() {
        File[] stagedFiles = STAGING_DIR.listFiles();
        if (stagedFiles != null) {
            for (File file : stagedFiles) {
                file.delete();
            }
        }
    }

    /** 获取当前分支名称 */
    private static String getCurrentBranch() {
        String headContent = Utils.readContentsAsString(HEAD_FILE);
        if (headContent.startsWith("ref: ")) {
            return headContent.substring(5).replace("refs/heads/", "");
        }
        return "主分支"; // 默认分支
    }
}
