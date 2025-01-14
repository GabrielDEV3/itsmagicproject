import org.json.JSONObject;

public class Git extends FilesPanelFileMenu {

  public Git() {
    super("Github/Synchronize");
  }

  public void onClick(File file) {
    String fileName = file.getName();
    if (fileName.endsWith(".git") || fileName.endsWith(".gitconfig") || fileName.endsWith(".gitignore")) {
      File gitFolder = file.getParentFile();
      File gitConfig = new File(gitFolder, ".gitconfig");
      File gitIgnore = new File(gitFolder, ".gitignore");
      File gitSync = new File(gitFolder, ".sync");

      if (gitConfig.exists()) {
        try {
          String source = FileLoader.loadTextFromFile(gitConfig);
          JSONObject json = new JSONObject(source);
          String owner = json.optString("owner", "");
          String repository = json.optString("repository", "");
          String accessToken = json.optString("accessToken", "");

          show("Repositório " + repository);
          if (gitIgnore.exists()) {
            show(".gitignore sendo processado.");
          } else {
            show(".gitignore não encontrado.");
          } 
          boolean success = generateSync(gitSync, gitIgnore);

          if (success) {
            GitMaster git = new GitMaster(owner, repository, accessToken);
            git.sync(gitSync);
          } else {
            show("Operação cancelada.");
          }
        } catch (Exception e) {
        }
      }
    } else {
      show("Use em arquivos .git apenas.");
    }
  }

  public static boolean generateSync(File gitSync, File gitIgnore) {
    try {
      List<String> ignoreFolders = new ArrayList<String>();
      List<String> ignoreExtensions = new ArrayList<String>();

      if (gitIgnore.exists()) {
        String source = FileLoader.loadTextFromFile(gitIgnore);

        String[] keys = source.split(",");
        for (String key : keys) {
          if (key.trim().startsWith(".")) {
            ignoreExtensions.add(key.trim());
          }
          if (key.trim().startsWith("/")) {
            ignoreFolders.add(key.trim().replaceFirst("/", ""));
          }
        }
      }
      List<String> paths = scanDirectory(new java.io.File(Directories.getProjectFolder()), ignoreFolders, ignoreExtensions);
      StringBuilder content = new StringBuilder();
      for (String path : paths) {
        content.append(path).append("\n");
      }
      // Console.log(content.toString());
      FileLoader.exportTextToFile(content.toString().trim(), gitSync);
      return true;

    } catch (Exception e) {
    }
    return false;
  }

  private static List<String> scanDirectory(java.io.File directory, List<String> ignoreFolders, List<String> ignoreExtensions) throws Exception {
    List<String> filesToSync = new ArrayList<String>();

    if (directory.isDirectory()) {
      String folderPath = directory.getAbsolutePath();

      for (String folder : ignoreFolders) {
        String folderName = new java.io.File(Directories.getProjectFolder(), folder).getAbsolutePath();
        if (folderName.equals(folderPath)) {
          return filesToSync;
        }
      }

      java.io.File[] files = directory.listFiles();
      if (files != null) {
        for (java.io.File file : files) {
          String filePath = file.getAbsolutePath();

          if (file.isDirectory()) {
            filesToSync.addAll(scanDirectory(file, ignoreFolders, ignoreExtensions));
          } else {
            String extension = getFileExtension(file);
            if (!ignoreExtensions.contains(extension)) {
              String relativePath = file.getAbsolutePath().replaceFirst(Directories.getProjectFolder(), "");
              filesToSync.add(relativePath + "[" + file.lastModified() + "]");
            }
          }
        }
      }
    }

    return filesToSync;
  }

  private static String getFileExtension(java.io.File file) {
    String name = file.getName();
    int i = name.lastIndexOf('.');
    if (i > 0) {
      return name.substring(i);
    } else {
      return "";
    }
  }

  public static class GitUser extends FilesPanelFileMenu {
    private File arquivo;
    private JSONObject json;
    private String usuarioProprietario, repositorio, tokenDeAcesso;

    public GitUser() {
      super("Github/Usuário");
    }

    public void onClick(File arquivo) {
      this.arquivo = arquivo;
      String nomeArquivo = arquivo.getName();
      if (nomeArquivo.endsWith(".gitconfig")) {
        try {
          String conteudo = FileLoader.loadTextFromFile(arquivo);
          try {
            json = new JSONObject(conteudo);
          } catch (Exception e) {
            json = new JSONObject();
          }

          usuarioProprietario = json.optString("owner", "");
          repositorio = json.optString("repository", "");
          tokenDeAcesso = json.optString("accessToken", "");

          // Perguntar pelo 'tokenDeAcesso', 'repositorio' e 'usuarioProprietario' em ordem invertida
          newInputDialog(
              "Token de acesso",
              tokenDeAcesso,
              new InputDialogListener() {
                public void onCancel() {
                  show("Cancelado");
                }

                public void onFinish(String texto) {
                  tokenDeAcesso = texto;
                  salvarJson();
                }
              });

          newInputDialog(
              "Repositório",
              repositorio,
              new InputDialogListener() {
                public void onCancel() {
                  show("Cancelado");
                }

                public void onFinish(String texto) {
                  repositorio = texto;
                  salvarJson();
                }
              });

          newInputDialog(
              "Nome de usuário do proprietário",
              usuarioProprietario,
              new InputDialogListener() {
                public void onCancel() {
                  show("Cancelado");
                }

                public void onFinish(String texto) {
                  usuarioProprietario = texto;
                  salvarJson();
                }
              });

        } catch (Exception e) {
          e.printStackTrace(); // Adiciona um log de erro para depuração
        }
      } else if (nomeArquivo.endsWith(".gitignore")) {
        show("Não é um .gitconfig.");
      } else {
        show("Use arquivos .git apenas.");
      }
    }

    public void salvarJson() {
      try {
        json.put("owner", usuarioProprietario);
        json.put("repository", repositorio);
        json.put("accessToken", tokenDeAcesso);
        FileLoader.exportTextToFile(json.toString(4), arquivo);
      } catch (Exception e) {
        e.printStackTrace(); // Adiciona um log de erro para depuração
      }
    }
  }

  public static class GitIgnore extends FilesPanelFileMenu {
    public GitIgnore() {
      super("Github/Ignore");
    }

    public void onClick(final File file) {
      String fileName = file.getName();
      if (fileName.endsWith(".gitignore")) {
        try {
          String source = FileLoader.loadTextFromFile(file);
          newInputDialog(
              "Ignore",
              source,
              new InputDialogListener() {
                public void onCancel() {
                  show("Cancelled");
                }

                public void onFinish(String text) {
                  try {
                    FileLoader.exportTextToFile(text, file);
                  } catch (Exception e) {
                  }
                }
              });
        } catch (Exception e) {
        }

      } else if (fileName.endsWith(".gitconfig")) {
        show("Não é um .gitignore.");
      } else {
        show("Use em arquivos .git apenas.");
      }
    }
  }

  private static void newInputDialog(String title, String text, InputDialogListener listener) {
    new InputDialog(title, text, "cancel", "ok", listener);
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
