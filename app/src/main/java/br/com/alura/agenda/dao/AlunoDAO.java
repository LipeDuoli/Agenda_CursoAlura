package br.com.alura.agenda.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.alura.agenda.modelo.Aluno;

public class AlunoDAO extends SQLiteOpenHelper {
    public AlunoDAO(Context context) {
        super(context, "Agenda", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Alunos (id CHAR(36) PRIMARY KEY, " +
                "nome TEXT NOT NULL, " +
                "endereco TEXT, " +
                "telefone TEXT, " +
                "site TEXT, " +
                "nota REAL, " +
                "caminhoFoto TEXT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "";
        switch (oldVersion) {
            case 1:
                sql = "ALTER TABLE Alunos ADD COLUMN caminhoFoto TEXT";
                db.execSQL(sql); // indo para versao 2
            case 2:
                String tabelaNova = "CREATE TABLE Alunos_novo " +
                        "(id CHAR(36) PRIMARY KEY, " +
                        "nome TEXT NOT NULL, " +
                        "endereco TEXT, " +
                        "telefone TEXT, " +
                        "site TEXT, " +
                        "nota REAL, " +
                        "caminhoFoto TEXT);";
                db.execSQL(tabelaNova);

                String inserindoAlunosTabelaNova = "INSERT INTO Alunos_novo " +
                        "(id, nome, endereco, telefone, site, nota, caminhoFoto) " +
                        "SELECT id, nome, endereco, telefone, site, nota, caminhoFoto " +
                        "FROM Alunos";
                db.execSQL(inserindoAlunosTabelaNova);

                String removeTbelaAntiga = "DROP TABLE Alunos";
                db.execSQL(removeTbelaAntiga);

                String alteraNomeTabela = "ALTER TABLE Alunos_novo " +
                        "RENAME TO Alunos";
                db.execSQL(alteraNomeTabela);

                String buscaAlunos = "SELECT * FROM Alunos";
                Cursor cursor = db.rawQuery(buscaAlunos, null);

                String atualizaAluno = "UPDATE Alunos SET id=? WHERE id=?";
                List<Aluno> alunos = populaAlunos(cursor);
                for (Aluno a :
                        alunos) {
                    db.execSQL(atualizaAluno, new String[] {geraUUID(), a.getId()});
                }
        }

    }

    private String geraUUID() {
        return UUID.randomUUID().toString();
    }

    public void insere(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();

        if(aluno.getId() == null){
            aluno.setId(geraUUID());
        }

        ContentValues dados = pegaDadosDoAluno(aluno);

        db.insert("Alunos", null, dados);
    }

    @NonNull
    private ContentValues pegaDadosDoAluno(Aluno aluno) {
        ContentValues dados = new ContentValues();
        dados.put("id", aluno.getId());
        dados.put("nome", aluno.getNome());
        dados.put("endereco", aluno.getEndereco());
        dados.put("telefone", aluno.getTelefone());
        dados.put("site", aluno.getSite());
        dados.put("nota", aluno.getNota());
        dados.put("caminhoFoto", aluno.getCaminhoFoto());
        return dados;
    }

    public List<Aluno> buscaAlunos() {
        String sql = "SELECT * FROM Alunos;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);

        List<Aluno> alunos = populaAlunos(c);
        c.close();

        return alunos;
    }

    @NonNull
    private List<Aluno> populaAlunos(Cursor c) {
        List<Aluno> alunos = new ArrayList<Aluno>();
        while (c.moveToNext()) {
            Aluno aluno = new Aluno();
            aluno.setId(c.getString(c.getColumnIndex("id")));
            aluno.setNome(c.getString(c.getColumnIndex("nome")));
            aluno.setEndereco(c.getString(c.getColumnIndex("endereco")));
            aluno.setTelefone(c.getString(c.getColumnIndex("telefone")));
            aluno.setSite(c.getString(c.getColumnIndex("site")));
            aluno.setNota(c.getDouble(c.getColumnIndex("nota")));
            aluno.setCaminhoFoto(c.getString(c.getColumnIndex("caminhoFoto")));

            alunos.add(aluno);
        }
        return alunos;
    }

    public void deleta(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();

        String[] params = {aluno.getId().toString()};
        db.delete("Alunos", "id = ?", params);
    }

    public void altera(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues dados = pegaDadosDoAluno(aluno);

        String[] params = {aluno.getId().toString()};
        db.update("Alunos", dados, "id = ?", params);
    }

    public boolean ehAluno(String telefone) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM Alunos WHERE telefone = ?", new String[]{telefone});
        int resultados = c.getCount();
        c.close();
        return resultados > 0;
    }

    public void sincroniza(List<Aluno> alunos) {
        for (Aluno a :
                alunos) {
            if (existe(a)){
                if(a.estaDesativado()){
                    deleta(a);
                } else {
                    altera(a);
                }
            } else if(!a.estaDesativado()) {
                insere(a);
            }
        }
    }

    private boolean existe(Aluno aluno) {
        SQLiteDatabase db = getReadableDatabase();
        String existeAluno = "SELECT id FROM Alunos WHERE id=? LIMIT 1";
        Cursor cursor = db.rawQuery(existeAluno, new String[]{aluno.getId()});
        int count = cursor.getCount();
        return count > 0;
    }
}
