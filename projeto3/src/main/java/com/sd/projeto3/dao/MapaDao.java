package com.sd.projeto3.dao;

import com.sd.projeto3.model.Mapa;
import com.sd.projeto3.util.SQLiteConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MapaDao implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static SnapshotDao snapshotDao = new SnapshotDao();
    
    public Mapa buscarPorId(Integer id) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = SQLiteConnection.connect();

            ps = con.prepareStatement("select chave, texto, tipo, data "
                    + "from mapa where chave = ?");
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            Mapa mapa = new Mapa();

            while (rs.next()) {
                mapa.setChave(rs.getInt("chave"));
                mapa.setTexto(rs.getString("texto"));
                mapa.setTipoOperacaoId(rs.getInt("tipo"));
                mapa.setData(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("data")));
            }
            rs.close();
            con.close();

            return mapa;

        } catch (SQLException | ParseException e) {
            throw new Exception("Erro ao buscar Mapa. " + e.getMessage());
        }
    }

    public Mapa salvar(Mapa mapa) throws Exception {
        
        Integer snapshotid = snapshotDao.retornarIdUltimoSnapshot();
        
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = SQLiteConnection.connect();

            ps = con.prepareStatement(
                    "insert into mapa(chave, texto, tipo, data, snapshotid) "
                    + "VALUES (?, ?, ?, datetime('now', 'localtime'), ?)");
            ps.setInt(1, mapa.getChave());
            ps.setString(2, mapa.getTexto());
            ps.setInt(3, mapa.getTipoOperacaoId());
            ps.setInt(4, snapshotid);

            if (ps.executeUpdate() == 0) {
                return null;
            }

            ResultSet rs = ps.getGeneratedKeys();
            mapa.setId(rs.getInt(1));

            Mapa m = buscarPorId(mapa.getChave());

            return m;

        } catch (SQLException e) {
            throw new Exception("Erro ao inserir mapa. " + e.getMessage());
        } finally {
            con.close();
        }

    }

    public Mapa editar(Mapa mapa) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;

        try {

            if (mapa.getChave() <= 0) {
                return null;
            }

            con = SQLiteConnection.connect();
            ps = con.prepareStatement("update mapa set texto = ?, tipo = ?, data = datetime('now', 'localtime') where chave = ? and snapshotid = (select id from snapshot order by data desc limit 1)");
            ps.setString(1, mapa.getTexto());
            ps.setInt(2, mapa.getTipoOperacaoId());
            ps.setInt(3, mapa.getChave());

            if (ps.executeUpdate() == 0) {
                return null;
            }

            Mapa m = buscarPorId(mapa.getChave());

            return m;

        } catch (SQLException e) {
            throw new Exception("Erro ao atualizar mapa. " + e.getMessage());
        } finally {
            con.close();
        }
    }

    public Mapa excluir(int id) throws Exception {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = SQLiteConnection.connect();
            ps = con.prepareStatement("delete from mapa where chave = ? and snapshotid = (select id from snapshot order by data desc limit 1)");
            ps.setInt(1, id);

            Mapa m = buscarPorId(id);

            if (ps.executeUpdate() > 0) {
                return m;
            } else {
                return null;
            }

        } catch (SQLException e) {
            throw new Exception("Erro ao excluir mapa. " + e.getMessage());
        } finally {
            con.close();
        }

    }

    public List<Mapa> buscarTodos() throws Exception {
        Connection con = null;

        try {
            con = SQLiteConnection.connect();
            PreparedStatement pstmt = con
                    .prepareStatement("select chave, texto, tipo, data "
                            + "from mapa m "
                            + "where snapshotid = (select id from snapshot order by data desc limit 1) "
                            + "order by data");

            ResultSet rs = pstmt.executeQuery();

            List<Mapa> mapas = new ArrayList<Mapa>();

            while (rs.next()) {
                Mapa mapa = new Mapa();

                mapa.setChave(rs.getInt("chave"));
                mapa.setTexto(rs.getString("texto"));
                mapa.setTipoOperacaoId(rs.getInt("tipo"));
                mapa.setData(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("data")));

                mapas.add(mapa);
            }

            con.close();

            return mapas;

        } catch (SQLException | ParseException e) {
            throw new Exception("Erro ao buscar lista de mapas. " + e.getMessage());
        }

    }
    
}
