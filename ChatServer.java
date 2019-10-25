//dropbox
package com.wsudesc.app;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
//listas
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
//arquivos
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
//sleep
import java.lang.InterruptedException;

public class ChatServer{

private static final String ACCESS_TOKEN = "jCTvGLwEWrkAAAAAAAABAyZMA7k-2nfI8a8VbflYe3I7BI3iyCHqmcKzYCxGtM5M";
private static final String dropboxDir = "dropbox_chat", clienteEntrada="f_entrada", clienteSaida = "f_saida", server_dir="server_dir", extensaoClientEntrada=".client", extensaoClientSaida=".chat", extensaoServer=".serv";
//server_dir é o nome do diretorio do servidor local e dropbox (mesmo nome)

// Create Dropbox client
public static DbxRequestConfig config;
public static DbxClientV2 client;
// Get current account info
public static FullAccount account;

  public List<String> listarDiretoriosDropboxChat_clientes() throws DbxException, IOException{
    ArrayList<String> all = new ArrayList<String>();

    // Get files and folder metadata from Dropbox root directory
    ListFolderResult result = client.files().listFolder("/"+dropboxDir);
    while (true) {
        for (Metadata metadata : result.getEntries()) {
            //System.out.println(metadata.getPathLower());
            all.add(metadata.getPathLower());
        }

        if (!result.getHasMore()) {
            break;
        }

        result = client.files().listFolderContinue(result.getCursor());
    }

    return all;
  }

  public List<String> listarArquivosDiretorioDropbox(String diretorio) throws DbxException, IOException{
    //receber o diretorio no padrao : /diretorio

    ArrayList<String> all = new ArrayList<String>();

    // Get files and folder metadata from Dropbox root directory
    ListFolderResult result = client.files().listFolder("/"+diretorio);
    while (true) {
        for (Metadata metadata : result.getEntries()) {
            //System.out.println(metadata.getPathLower());
            all.add(metadata.getPathLower());
        }

        if (!result.getHasMore()) {
            break;
        }

        result = client.files().listFolderContinue(result.getCursor());
    }

      return all;
  }

  public void deletarArquivoDropbox(String arquivo) throws DbxException, IOException{
    Metadata metadata = client.files().delete(arquivo);
  }

  public void baixarArquivoDropbox(String arquivo, String destino, String extensao) throws DbxException, IOException{

    DbxDownloader<FileMetadata> downloader = client.files().download(arquivo);
      try {
        System.out.println("baixando: "+arquivo);

         String [] div=arquivo.split("/");//arquivo = /dropbox../f_saida_../nomeArquivo.extensao::: extrair o arquivo

         String nomeArquivo = div[div.length-1];
         nomeArquivo=nomeArquivo.replace(extensaoClientSaida, extensao);

          FileOutputStream out = new FileOutputStream(destino+"/"+nomeArquivo);///////////////aqui alterar a extensao .serv para download no server
          downloader.download(out);
          out.close();
      } catch (DbxException ex) {
          System.out.println(ex.getMessage());
      }
  }

  public void uploadArquivoDropbox(String arquivo, String nomeCliente, String destino, String extensao) throws DbxException, IOException{
     try {

         String [] div=arquivo.split("/");//arquivo = /dropbox../f_saida_../nomeArquivo.extensao::: extrair o arquivo
         String nomeArquivo = div[div.length-1];

         //   nomeArquivo=nomeArquivo.replace(extensaoClientS, extensaoServer);
         // }
         nomeArquivo=nomeArquivo.replace(extensaoClientSaida, extensaoServer);


         System.out.println("upload: "+server_dir+"/"+nomeCliente+"/"+nomeArquivo+" :: destino: "+destino+"/"+nomeArquivo);
         // if(extensao.compareTo(extensaoClientEntrada)!=0){
         InputStream in = new FileInputStream(server_dir+"/"+nomeCliente+"/"+nomeArquivo);
         FileMetadata metadata = client.files().uploadBuilder(destino+"/"+nomeArquivo.replace(extensaoServer,extensaoClientEntrada)).uploadAndFinish(in);
     }catch (DbxException ex) {
          System.out.println(ex.getMessage());
      }

  }

  public static void main(String[] args) throws DbxException, IOException{

    // Create Dropbox client
    config = new DbxRequestConfig(dropboxDir, "en_US");
    client = new DbxClientV2(config, ACCESS_TOKEN);

    // Get current account info
    account = client.users().getCurrentAccount();
    //System.out.println(account.getName().getDisplayName());

    //criar server dir
    File diretorioServer = new File(server_dir);
    if(!diretorioServer.exists()){
        diretorioServer.mkdir();
    }

    ChatServer cs = new ChatServer();

while(true){
    System.out.println("Verificando atualizacoes no servidor....Nao fechar!");
    List<String> diretoriosDropbox = cs.listarDiretoriosDropboxChat_clientes();
    List<String> clientes = new ArrayList<String>();

    //encontrar os clientes que ja acessaram o dropbox
    for(String dir : diretoriosDropbox){
// System.out.println(dir);
      if(dir.contains(clienteEntrada)){
        clientes.add(dir.split(clienteEntrada+"_")[1]);// /f_entrada_nomeCliente
        // System.out.println("add cliente: "+dir.split(clienteEntrada+"_")[1]);
      }
    }

  //tenho os nomes dos clientes em clientes
  //criar o diretorio fisico em server_dir
  for(String cl : clientes){
    File diretorio = new File(server_dir+"/"+cl);
    if(!diretorio.exists()){
        diretorio.mkdir();
    }

    List<String> clienteArquivos = cs.listarArquivosDiretorioDropbox("/"+dropboxDir+"/"+clienteSaida+"_"+cl);//retorna o caminho completo de cada arquivo

    for(String ca : clienteArquivos){
      cs.baixarArquivoDropbox(ca,server_dir+"/"+cl,extensaoServer);
      cs.deletarArquivoDropbox(ca);

      for(String cl1 : clientes){

        if(cl1.compareTo(cl)==0){//nao enviar para o mesmo cliente de que se foi baixado
          cs.uploadArquivoDropbox(ca,cl,"/"+dropboxDir+"/"+server_dir+"/"+cl, extensaoServer);
          continue;
        }

        cs.uploadArquivoDropbox(ca,cl,"/"+dropboxDir+"/"+clienteEntrada+"_"+cl1, extensaoClientEntrada);
      }

    }

  }

  System.out.println("Tudo pronto!");
try{
Thread.sleep(10000);
}catch (InterruptedException ex) { System.out.println(ex); }
}

  }

}

/**
Chat server:

- baixa o que existe em f_saida de cada cliente
- faz re-upload em f_entrada e em server_dir


*/
