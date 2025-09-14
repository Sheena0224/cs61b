package gitlet;

// TODO: any imports you need here
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Date; // TODO: You'll likely use this in this class

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    /** 提交时间戳 */
    private Date timestamp;
    /** 提交跟踪的文件映射（文件名 -> blob哈希值） */
    private HashMap<String, String> trackedFiles;
    /** 提交信息 */
    private String message;
    /** 父提交的哈希值 */
    private String parent;

    /** 设置父提交哈希 */
    public void setParent(String parentHash) {
        this.parent = parentHash;
    }
    /** 设置跟踪的文件映射 */
    public void setTrackedFiles(HashMap<String, String> trackedFiles) {
        this.trackedFiles = trackedFiles;
    }
    /** 获取父提交哈希 */
    public String getParent() {
        return parent;
    }

    /* TODO: fill in the rest of this class. */
    /** 默认构造函数，用于创建初始提交 */
    public Commit() {
        this.message = "";
        this.timestamp = new Date(0); // Unix纪元时间
        this.trackedFiles = new HashMap<>(); // 初始提交不包含任何文件
    }

    /** 设置提交消息 */
    public void setMessage(String message) {
        this.message = message;
    }

    /** 设置提交时间戳 */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /** 保存提交对象到文件系统 */
    public void save() {
        // 序列化提交对象
        byte[] serialized = Utils.serialize(this);
        // 计算SHA-1哈希值
        String hash = Utils.sha1(serialized);

        // 创建提交文件（使用哈希值作为文件名）
        File commitFile = Utils.join(Main.COMMITS_DIR, hash);
        Utils.writeObject(commitFile, this);
    }

    /** 获取跟踪的文件映射 */
    public HashMap<String, String> getTrackedFiles() {
        return trackedFiles;
    }

    /** 根据哈希值获取提交对象 */
    private static Commit getCommit(String hash) {
        File commitFile = Utils.join(Main.COMMITS_DIR, hash);
        return Utils.readObject(commitFile, Commit.class);
    }

    /** 获取提交消息 */
    public String getMessage() {
        return message;
    }

    /** 获取时间戳 */
    public Date getTimestamp() {
        return timestamp;
    }
}
