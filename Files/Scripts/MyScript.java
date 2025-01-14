void start() {

  String base64Content = "RmlsZXMvZ2l0Ly5zeW5jWzBdCg=="; // Sua string Base64

  try {
    // Decodificando o conteúdo
    byte[] decodedBytes = Base64.getDecoder().decode(base64Content);

    // Convertendo para string (se for texto legível)
    String decodedString = new String(decodedBytes);
    Console.log(decodedString);
  } catch (Exception e) {

  } 
  /*
  GitMaster gitMaster = new GitMaster();

  List<String> localSync = new ArrayList<String>();
  localSync.add("path/to/file1[1678890000000]");
  localSync.add("path/to/file2[1678890500000]");
  localSync.add("path/to/file3[1678890700000]");

  List<String> remoteSync = new ArrayList<String>();
  remoteSync.add("path/to/file1[1678890100000]");
  remoteSync.add("path/to/file2[1678890000000]");
  remoteSync.add("path/to/file4[1678890800000]");

  List<String> filesToDownload = gitMaster.interceptFilesToDownload(localSync, remoteSync);
  Console.log("Arquivos para download:");
  for (String file : filesToDownload) {
    Console.log(file);
  }

  List<String> filesToUpload = gitMaster.interceptFilesToUpload(localSync, remoteSync);
  Console.log("\nArquivos para upload:");
  for (String file : filesToUpload) {
    Console.log(file);
  }*/
}