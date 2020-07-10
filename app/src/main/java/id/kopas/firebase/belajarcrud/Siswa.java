package id.kopas.firebase.belajarcrud;

public class Siswa {
    public String nama;
    public String alamat;
    public String foto;
    public String key;

    public void setKey(String key) {
        this.key = key;
    }

    public Siswa(){}
    public Siswa(String nama, String alamat, String foto){
        this.nama =  nama;
        this.alamat = alamat;
        this.foto = foto;
    }
}
