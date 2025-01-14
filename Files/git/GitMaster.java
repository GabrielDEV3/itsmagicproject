import java.util.function.Consumer;
import org.json.JSONObject;

public class GitMaster {

  private String owner, repo, token;

  public GitMaster(String owner, String repo, String token) {
    this.owner = owner;
    this.repo = repo;
    this.token = token;
  }

  public void sync(File local) {
    try {
      String localString = FileLoader.loadTextFromFile(local);

      final List<String> localSync = new ArrayList<String>();

      for (String path : localString.split("\n")) {
        localSync.add(path);
      }

      getSyncPath(
          new Consumer<List<String>>() {
            public void accept(List<String> remoteSync) {
              try {

                List<String> download = interceptFilesToDownload(localSync, remoteSync);
                List<String> upload = interceptFilesToUpload(localSync, remoteSync);
                final float[] time = new float[1];

                show((download.size() + upload.size()) + " Total");

                startDownload(
                    download,
                    new Consumer<Snapshot>() {
                      public void accept(Snapshot snapshot) {
                        time[0] += Time.deltaTime();
                        if (time[0] >= 0.5f) {
                          time[0] = 0;
                          show("Download: " + snapshot.getCurrent() + "/" + snapshot.getTotal());
                       } 
                      }
                    });
                   show("Download feito com sucesso");
                startUpload(
                    upload,
                    new Consumer<Snapshot>() {
                      public void accept(Snapshot snapshot) {
                        time[0] += Time.deltaTime();
                        if (time[0] >= 0.5f) {
                          time[0] = 0;
                          show("Upload: " + snapshot.getCurrent() + "/" + snapshot.getTotal());
                        }
                      }
                    });
                   
                   show("Upload feito com sucesso");
              } catch (Exception e) {
                Console.log(e);
              }
            }
          });
    } catch (Exception e) {
      Console.log(e);
    }
  }

  public void getSyncPath(final Consumer<List<String>> consumer) {
    new Thread(
            new Runnable() {
              public void run() {
                try {
                  GitClone.context = Directories.getProjectFolder();
                  GitClone clone = new GitClone("Files/git/.sync", owner, repo, token);

                  File remoteFile = new File(Directories.getProjectFolder(), "Files/git/cache/.sync");
                  clone.setOutputFile(remoteFile);

                  clone.commit();

                  String remoteString = FileLoader.loadTextFromFile(remoteFile);

                  List<String> remoteSync = new ArrayList<String>();
                  for (String path : remoteString.split("\n")) {
                    remoteSync.add(path);
                  }

                  consumer.accept(remoteSync);
                } catch (Exception e) {
                  Console.log(e);
                }
              }
            })
        .start();
  }

  public void setSyncPath(final Consumer<Boolean> consumer) {
    new Thread(
        new Runnable() {
          public void run() {}
        });
  }

  private List<String> interceptFiles(List<String> localSync, List<String> remoteSync, boolean isDownload) {
    List<String> files = new ArrayList<String>();

    for (String remoteEntry : remoteSync) {
      String remotePath = extractPath(remoteEntry);
      long remoteTimestamp = extractTimestamp(remoteEntry);

      String localEntry = null;
      for (String entry : localSync) {
        if (extractPath(entry).equals(remotePath)) {
          localEntry = entry;
          break;
        }
      }

      if (localEntry == null && isDownload) {
        files.add(remotePath);
      } else if (localEntry != null) {
        long localTimestamp = extractTimestamp(localEntry);
        if (isDownload && remoteTimestamp > localTimestamp) {
          files.add(remotePath);
        } else if (!isDownload && localTimestamp > remoteTimestamp) {
          files.add(remotePath);
        }
      }
    }

    if (!isDownload) {
      for (String localEntry : localSync) {
        String localPath = extractPath(localEntry);

        boolean existsInRemote = false;
        for (String entry : remoteSync) {
          if (extractPath(entry).equals(localPath)) {
            existsInRemote = true;
            break;
          }
        }

        if (!existsInRemote) {
          files.add(localPath);
        }
      }
    }

    return files;
  }

  private String extractPath(String entry) {
    return entry.substring(0, entry.lastIndexOf('['));
  }

  private long extractTimestamp(String entry) {
    String timestamp = entry.substring(entry.lastIndexOf('[') + 1, entry.lastIndexOf(']'));
    return Long.parseLong(timestamp);
  }

  public List<String> interceptFilesToDownload(List<String> localSync, List<String> remoteSync) {
    return interceptFiles(localSync, remoteSync, true);
  }

  public List<String> interceptFilesToUpload(List<String> localSync, List<String> remoteSync) {
    return interceptFiles(localSync, remoteSync, false);
  }

  public void startDownload(final List<String> filesToDownload, final Consumer<Snapshot> consumer) {
    int total = filesToDownload.size();
    Snapshot snapshot = new Snapshot(total);
    for (String file : filesToDownload) {
      GitClone.context = Directories.getProjectFolder();
      GitClone clone = new GitClone(file, owner, repo, token);
      snapshot.next();
      consumer.accept(snapshot);
    }
  }

  public void startUpload(final List<String> filesToUpload, final Consumer<Snapshot> consumer) {
    int total = filesToUpload.size();
    Snapshot snapshot = new Snapshot(total);
    for (String file : filesToUpload) {
      Console.log(file);
      GitPush.context = Directories.getProjectFolder();
      GitPush push = new GitPush(file, owner, repo, token);
      push.commit("Update changes");
      snapshot.next();
      consumer.accept(snapshot);
    }
  }

  public static class Snapshot {
    private int current;
    private int total;

    public Snapshot(int total) {
      this.total = total;
    }

    public void next() {
      current++;
    }

    public boolean isComplete() {
      return current >= total;
    }

    public int getTotal() {
      return total;
    }

    public int getCurrent() {
      return current;
    }
  }

  public static void show(final String s) {
    Thread.runOnEngine(
        new Runnable() {
          public void run() {
            Toast.showText(s, 0);
          }
        });
  }
}
