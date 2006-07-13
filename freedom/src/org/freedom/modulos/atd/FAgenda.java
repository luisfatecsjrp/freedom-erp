/**
 * @version 05/06/2003 <BR>
 * @author Setpoint Inform�tica Ltda./Anderson Sanchez <BR>
 *
 * Projeto: Freedom <BR>
 *  
 * Pacote: org.freedom.modulos.atd <BR>
 * Classe: @(#)FAgenda.java <BR>
 * 
 * Este programa � licenciado de acordo com a LPG-PC (Licen�a P�blica Geral para Programas de Computador), <BR>
 * vers�o 2.1.0 ou qualquer vers�o posterior. <BR>
 * A LPG-PC deve acompanhar todas PUBLICA��ES, DISTRIBUI��ES e REPRODU��ES deste Programa. <BR>
 * Caso uma c�pia da LPG-PC n�o esteja dispon�vel junto com este Programa, voc� pode contatar <BR>
 * o LICENCIADOR ou ent�o pegar uma c�pia em: <BR>
 * Licen�a: http://www.lpg.adv.br/licencas/lpgpc.rtf <BR>
 * Para poder USAR, PUBLICAR, DISTRIBUIR, REPRODUZIR ou ALTERAR este Programa � preciso estar <BR>
 * de acordo com os termos da LPG-PC <BR> <BR>
 *
 * Tela para manuten��o da agenda de usu�rios
 * 
 */

package org.freedom.modulos.atd;

import java.util.List;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.Calendar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.JOptionPane;
import org.freedom.componentes.JPanelPad;
import javax.swing.JScrollPane;
import org.freedom.componentes.JTabbedPanePad;
import net.sf.nachocalendar.components.CalendarPanel;
import net.sf.nachocalendar.tasks.TaskCalendarFactory;
import org.freedom.bmps.Icone;
import org.freedom.componentes.GuardaCampo;
import org.freedom.componentes.JButtonPad;
import org.freedom.componentes.JTextFieldFK;
import org.freedom.componentes.JTextFieldPad;
import org.freedom.componentes.ListaCampos;
import org.freedom.componentes.Tabela;
import org.freedom.funcoes.Funcoes;
import org.freedom.telas.Aplicativo;
import org.freedom.telas.FFilho;

import bizcal.common.CalendarViewConfig;
import bizcal.common.DayViewConfig;
import bizcal.common.Event;
import bizcal.swing.DayView;
import bizcal.swing.MonthView;
import bizcal.util.DateInterval;
import bizcal.util.DateUtil;
import bizcal.common.CalendarModel;
import bizcal.common.Calendar;

public class FAgenda extends FFilho implements ActionListener {
  private static final long serialVersionUID = 1L;	
  private JPanelPad pinCabAgd = new JPanelPad(0,40);
  private JPanelPad pnAgd = new JPanelPad(JPanelPad.TP_JPANEL,new BorderLayout());
  private JPanelPad pnCalendar = new JPanelPad(JPanelPad.TP_JPANEL,new BorderLayout());
  private JPanelPad pnFiltros = new JPanelPad(JPanelPad.TP_JPANEL);
  private JPanelPad pnRodAgd = new JPanelPad(JPanelPad.TP_JPANEL,new BorderLayout());
  private JTabbedPanePad tpnAgd = new JTabbedPanePad();
  private Tabela tabAgd = new Tabela();
  private JTabbedPanePad tpnVisoes = new JTabbedPanePad();
  private CalendarPanel calendarpanel  = TaskCalendarFactory.createCalendarPanel(1);
  private JScrollPane spnAgd = new JScrollPane(tpnVisoes);  
  private JTextFieldPad txtIdUsu = new JTextFieldPad(JTextFieldPad.TP_STRING,8,0);
  private JTextFieldFK txtNomeUsu = new JTextFieldFK(JTextFieldPad.TP_STRING,50,0);
  private JButtonPad btExec = new JButtonPad(Icone.novo("btExecuta.gif"));
  private JButtonPad btNovo = new JButtonPad(Icone.novo("btNovo.gif"));
  private JButtonPad btExcluir = new JButtonPad(Icone.novo("btExcluir.gif"));
  private JButtonPad btSair = new JButtonPad("Sair",Icone.novo("btSair.gif"));
  private ListaCampos lcUsu = new ListaCampos(this);
  private Vector vCodAgds = new Vector();
  private MonthView Mes = null;
  private DayView Dia = null;
   
  
  int iCodAge = 0;
  String sTipoAge = "";
  
  public FAgenda() {
  	super(false);
 
  	try {
		Dia = new DayView(new DayViewConfig());
		Dia.setModel(new ThisModel());
		Dia.refresh();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
	}

  	try {
		Mes = new MonthView(new CalendarViewConfig());
		Dia.setModel(new ThisModel());
		Dia.refresh();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
	}

  	txtIdUsu.setVisible(false);
  	txtIdUsu.setVlrString(Aplicativo.strUsuario);
  	
  	setTitulo("Agenda");
  	setAtribos(10,10,760,400);
  	  	
	lcUsu.add(new GuardaCampo( txtIdUsu, "IdUsu", "ID Usuario", ListaCampos.DB_PK, false));
	lcUsu.add(new GuardaCampo( txtNomeUsu, "NomeUsu", "Nome", ListaCampos.DB_SI,false));
	lcUsu.montaSql(false, "USUARIO", "SG");    
	lcUsu.setReadOnly(true);
	txtIdUsu.setTabelaExterna(lcUsu);
	txtIdUsu.setFK(true);
	txtIdUsu.setNomeCampo("IdUsu");
    
  	tpnAgd.add("Agenda do usu�rio",pnAgd);
  	tpnVisoes.add("   Lista de eventos  ",tabAgd);
  	tpnVisoes.add("   Dia   ",Dia.getComponent());

  	pnAgd.add(pinCabAgd,BorderLayout.NORTH);
  	
  	calendarpanel.setAntiAliased(true);
  	calendarpanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
  	
  		tabAgd.addMouseListener(
			  new MouseAdapter() {
			  	public void mouseClicked(MouseEvent mevt) {
			  	  if (mevt.getClickCount() == 2) {
			  	  	editaAgd();
			  	  }
			  	}
			  }
			);

	
//  	pnAgd.add(calendarpanel,BorderLayout.WEST);
  		
  		pnCalendar.add(calendarpanel,BorderLayout.NORTH);
  		pnCalendar.add(pnFiltros,BorderLayout.CENTER);
  		
  		pnAgd.add(pnCalendar,BorderLayout.WEST);

  	pnAgd.add(spnAgd,BorderLayout.CENTER);

  	getTela().add(tpnAgd);
	
	txtNomeUsu.setBackground(this.getBackground());	
	txtNomeUsu.tiraBorda();
	pinCabAgd.adic(txtNomeUsu,7,10,250,20);
		
	tabAgd.adicColuna("Ind.");
	tabAgd.adicColuna("Sit.");
	tabAgd.adicColuna("Data ini.");
	tabAgd.adicColuna("Hora ini.");
	tabAgd.adicColuna("Data fim.");
	tabAgd.adicColuna("Hora fim.");
	tabAgd.adicColuna("Assunto");
	
	tabAgd.setTamColuna(40,0);
	tabAgd.setTamColuna(20,1);
	tabAgd.setTamColuna(80,2);
	tabAgd.setTamColuna(60,3);
	tabAgd.setTamColuna(80,4);
	tabAgd.setTamColuna(60,5);
	tabAgd.setTamColuna(160,6);
	
	JPanelPad pnBot = new JPanelPad(JPanelPad.TP_JPANEL,new GridLayout(1,2));
	pnBot.setPreferredSize(new Dimension(90,30));
	pnBot.add(btNovo);
	pnBot.add(btExcluir);
	pnBot.add(btExec);
	
	pnRodAgd.add(pnBot,BorderLayout.WEST);
	
	btSair.setPreferredSize(new Dimension(110,30));        
	pnRodAgd.add(btSair,BorderLayout.EAST);
	
	btSair.addActionListener(this);
	
	tabAgd.addMouseListener(
	  new MouseAdapter() {
	  	public void mouseClicked(MouseEvent mevt) {
	  	  if (mevt.getClickCount() == 2) {
	  	  	editaAgd();
	  	  }
	  	}
	  }
	);
	
	pnAgd.add(pnRodAgd,BorderLayout.SOUTH);
	
	btNovo.addActionListener(this);
	btExcluir.addActionListener(this);
	btExec.addActionListener(this);

}
  
  private void buscaAgente() {

  	String sSQL = "SELECT U.CODAGE,U.TIPOAGE FROM SGUSUARIO U WHERE CODEMP=? AND CODFILIAL=? " +
  			  	  "AND IDUSU=?";
  	try {
  		PreparedStatement ps = con.prepareStatement(sSQL);
  		ps.setInt(1,Aplicativo.iCodEmp);
  		ps.setInt(2,Aplicativo.iCodFilial);
  		ps.setString(3,Aplicativo.strUsuario);
  		
  		ResultSet rs = ps.executeQuery();
  		while (rs.next()) {
  			iCodAge = rs.getInt(1);
  			sTipoAge = rs.getString(2);  			
  		}  		
  	}
  	catch(Exception e){
  		e.printStackTrace();
  	}
  }
  private void carregaTabAgd() {
    buscaAgente();
  	if (iCodAge!=0) {
		Object[] oDatas = calendarpanel.getValues();
		Date dData = null;
		String sDatas = "";
  		if (oDatas == null) {
  			oDatas = new Object[1];
			oDatas[1] = new Date();
  		}	
  		
  		for (int i=0;oDatas.length>i;i++) {
  			dData = (Date) (oDatas[i]);
  			if(i==0)
  				sDatas = "'"+Funcoes.dateToStrDB(dData)+"'";
  			else
  				sDatas = sDatas + "," + "'"+Funcoes.dateToStrDB(dData)+"'";
  		}
  		
			
  		String sSQL = "SELECT A.CODAGD,A.SITAGD,A.DTAINIAGD,A.HRINIAGD,A.DTAFIMAGD,A.HRFIMAGD,A.ASSUNTOAGD" +
 		                  " FROM SGAGENDA A WHERE A.CODEMP=? AND A.CODFILIAL=? AND A.CODAGE=? AND A.TIPOAGE=?" +
// 		                  " AND DTAINIAGD BETWEEN ? AND ?" +
						  " AND DTAINIAGD IN("+sDatas+") "+
  		                  " ORDER BY A.DTAINIAGD DESC,A.HRINIAGD DESC,A.DTAFIMAGD DESC,A.HRFIMAGD DESC";
  		
  		System.out.println(sSQL);
  		try {
  			PreparedStatement ps = con.prepareStatement(sSQL);
  			ps.setInt(1,Aplicativo.iCodEmp);
  			ps.setInt(2,Aplicativo.iCodFilialPad);
  			ps.setInt(3,iCodAge);
  			ps.setString(4,sTipoAge);
  			
  			ResultSet rs = ps.executeQuery();
  			tabAgd.limpa();
  			vCodAgds.clear();
  			for (int i=0;rs.next();i++) {
  				vCodAgds.addElement(rs.getString("CodAgd")); 
  				tabAgd.adicLinha();
  				tabAgd.setValor(rs.getString("CodAgd"),i,0);
  				tabAgd.setValor(rs.getString("SitAgd"),i,1);
  				tabAgd.setValor(Funcoes.sqlDateToStrDate(rs.getDate("DtaIniAgd")),i,2);
  				tabAgd.setValor(rs.getString("HrIniAgd"),i,3);
  				tabAgd.setValor(Funcoes.sqlDateToStrDate(rs.getDate("DtaFimAgd")),i,4);
  				tabAgd.setValor(rs.getString("HrFimAgd"),i,5);
      		tabAgd.setValor(rs.getString("AssuntoAgd"),i,6);
  			}
  			rs.close();
  			ps.close();
  		}
  		catch (SQLException err) {
  			Funcoes.mensagemErro(this,"Erro ao carregar agenda!\n"+err.getMessage(),true,con,err);
  		}
  	}
  	else {
  		Funcoes.mensagemErro(this,"N�o existe agente para o objeto especificado!");
  	}
  }
  private void editaAgd() {
  	int iLin = 0;
  	if ((iLin = tabAgd.getLinhaSel()) < 0) {
  		Funcoes.mensagemInforma(this,"N�o ha nenhum agendamento selecionado!");
  		return;
  	}
  	String sSQL = "SELECT DTAINIAGD, HRINIAGD, DTAFIMAGD, HRFIMAGD," +
  			              "ASSUNTOAGD,DESCAGD,TIPOAGD,IDUSUD FROM SGAGENDA " +
  			              "WHERE CODEMP=? AND CODFILIAL=? AND CODAGD=?";
  	try {
  		PreparedStatement ps = con.prepareStatement(sSQL);
  		ps.setInt(1,Aplicativo.iCodEmp);
  		ps.setInt(2,ListaCampos.getMasterFilial("SGAGENDA"));
  		ps.setInt(3,Integer.parseInt((String)tabAgd.getValor(iLin,0)));
  		ResultSet rs = ps.executeQuery();
  		if (rs.next()) {
  			GregorianCalendar calIni = new GregorianCalendar();
  			GregorianCalendar calFim = new GregorianCalendar();
  			DLNovoAgen dl = new DLNovoAgen(rs.getString("IdUsuD"),(Date)(calendarpanel.getValue()),this);
  			dl.setConexao(con);
  			calIni.setTime(rs.getTime("HrIniAgd"));
  			calFim.setTime(rs.getTime("HrFimAgd"));
  			dl.setValores(
  					new String[] {
							   Funcoes.sqlDateToStrDate(rs.getDate("DtaIniAgd")),
							   Funcoes.strZero(""+calIni.get(java.util.Calendar.HOUR_OF_DAY),2)+":"+Funcoes.strZero(""+calIni.get(java.util.Calendar.MINUTE),2),

							   Funcoes.sqlDateToStrDate(rs.getDate("DtaFimAgd")),
							   Funcoes.strZero(""+calFim.get(java.util.Calendar.HOUR_OF_DAY),2)+":"+Funcoes.strZero(""+calIni.get(java.util.Calendar.MINUTE),2),
							   rs.getString("AssuntoAgd"),
							   rs.getString("DescAgd"),
							   rs.getString("TipoAgd"),
						   }
  			);
  			dl.setVisible(true);
  			if (dl.OK) {
  			String[] sRets = dl.getValores();
  				try {
  					sSQL = "EXECUTE PROCEDURE SGSETAGENDASP(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  					PreparedStatement ps2 = con.prepareStatement(sSQL);
					ps2.setInt(1,Integer.parseInt((String)tabAgd.getValor(iLin,0)));
  					ps2.setInt(2,Aplicativo.iCodEmp);
  					ps2.setDate(3,Funcoes.strDateToSqlDate(sRets[0]));
  					ps2.setString(4,sRets[1]+":00");
  					ps2.setDate(5,Funcoes.strDateToSqlDate(sRets[2]));
  					ps2.setString(6,sRets[3]+":00");
  					ps2.setString(7,sRets[4]);
  					ps2.setString(8,sRets[5]);
  					ps2.setString(9,sRets[6]);
  					ps2.setInt(10,5);
  					ps2.setInt(11,Aplicativo.iCodFilialPad);
  					ps2.setString(12,Aplicativo.strUsuario);
  					ps2.setString(13,sRets[7]);
  					ps2.setString(14,sRets[8]);
  					ps2.setInt(15,iCodAge);	
  					ps2.setString(16,sTipoAge);
  					  					
  					ps2.execute();
  					ps2.close();
  					
  					
  				}
  				catch(SQLException err) {
  					Funcoes.mensagemErro(this,"Erro ao salvar o agendamento!\n"+err.getMessage(),true,con,err);
  				}
  				carregaTabAgd();
  			}
  		    dl.dispose();
  		}
  		rs.close();
  		ps.close();
  		if (!con.getAutoCommit())
  			con.commit();
  	}
  	catch (SQLException err) {
  		Funcoes.mensagemErro(this,"Erro ao buscar informa��es!\n"+err.getMessage(),true,con,err);
  		err.printStackTrace();
  	}
  }	
  private void excluiAgd() {
  	if (tabAgd.getLinhaSel() == -1) { 
		Funcoes.mensagemInforma(this,"Selecione um item na lista!");
  	  return;
    } 
  	else if (Funcoes.mensagemConfirma(this,"Deseja relamente excluir o agendamento '"+vCodAgds.elementAt(tabAgd.getLinhaSel())+"'?") != JOptionPane.YES_OPTION) {
  		return;
  	}
    try {
	  String sSQL = "DELETE FROM SGAGENDA WHERE CODAGD=? AND CODEMP=? AND CODFILIAL=? AND CODAGE=? AND TIPOAGE=?";
	  PreparedStatement ps = con.prepareStatement(sSQL);
	  ps.setString(1,""+vCodAgds.elementAt(tabAgd.getLinhaSel()));
	  ps.setInt(2,Aplicativo.iCodEmp);
	  ps.setInt(3,ListaCampos.getMasterFilial("SGAGENDA"));
	  ps.setInt(4,iCodAge);
	  ps.setString(5,sTipoAge);
	  ps.execute();
	  ps.close();
	  if (!con.getAutoCommit())
	  	con.commit();
	}
	catch(SQLException err) {
		Funcoes.mensagemErro(this,"Erro ao excluir agendamento!\n"+err.getMessage(),true,con,err);
	}
	carregaTabAgd();
  }
  private void novoAgd() {
  	if (txtIdUsu.getVlrString().equals("") || (iCodAge==0)) {
		Funcoes.mensagemInforma(this,"O usu�rio ou o agente n�o foi identificado!");
  		return;
  	}
  	if (calendarpanel.getValue()==null) {
		Funcoes.mensagemInforma(this,"Selecione uma data no painel!");
  		return;
  	}
  	String sRets[];
  	DLNovoAgen dl = new DLNovoAgen(txtIdUsu.getVlrString(),(Date)(calendarpanel.getValue()),this);
  	dl.setConexao(con);
  	dl.setVisible(true);
  	if (dl.OK) {
  	  sRets = dl.getValores();
  	  try {
	    String sSQL = "EXECUTE PROCEDURE SGSETAGENDASP(0,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	    PreparedStatement ps = con.prepareStatement(sSQL);
	    ps.setInt(1,Aplicativo.iCodEmp);  
	    ps.setDate(2,Funcoes.strDateToSqlDate(sRets[0]));
	    ps.setString(3,sRets[1]+":00"); 
	    ps.setDate(4,Funcoes.strDateToSqlDate(sRets[2]));
	    ps.setString(5,sRets[3]+":00");
	    ps.setString(6,sRets[4]);
	    ps.setString(7,sRets[5]);
	    ps.setString(8,sRets[6]); 
	    ps.setInt(9,5);
	    ps.setInt(10,Aplicativo.iCodFilialPad);
	    ps.setString(11,Aplicativo.strUsuario); 
	    ps.setString(12,sRets[7]);
	    ps.setString(13,sRets[8]);  
	    ps.setInt(14,iCodAge);
	    ps.setString(15,sTipoAge);

	    ps.execute();
	    ps.close();
	    if (!con.getAutoCommit())
	    	con.commit();
      }
  	  catch(SQLException err) {
		Funcoes.mensagemErro(this,"Erro ao salvar o agendamento!\n"+err.getMessage(),true,con,err);
  	  }
	  carregaTabAgd();
    }
    dl.dispose();
  }

  public void actionPerformed(ActionEvent evt) {
  	if (evt.getSource() == btSair) {
		dispose();
  	}
  	else if (evt.getSource() == btNovo) {
  		novoAgd();
  	}
	else if (evt.getSource() == btExcluir) {
		excluiAgd();
	}

	else if (evt.getSource() == btExec) {
		carregaTabAgd();
	}
  }
  public void setConexao(Connection cn) {
  	super.setConexao(cn);
  	lcUsu.setConexao(cn);
    lcUsu.carregaDados();
    carregaTabAgd();
  }
  
  private static class ThisModel extends CalendarModel.BaseImpl {
	private List events = new ArrayList();
	private DateInterval interval;
	private Calendar cal;
	
	public ThisModel()
		throws Exception
	{
		Date date = DateUtil.round2Week(new Date());
		date = new Date(date.getTime() + 8*60*60*1000);
		for (int i=0; i < 7; i++) {
			Event event = new Event();
			event.setStart(date);
			event.setEnd(new Date(date.getTime() + 90*60*1000));
			event.setSummary("Test " + i);
			events.add(event);
			date = DateUtil.getDiffDay(date, +1);
			date = new Date(date.getTime() + 60*60*1000);
		}
		Date start = DateUtil.round2Week(new Date());
		Date end = DateUtil.getDiffDay(start, +7);
		interval = new DateInterval(start, end);
		cal = new Calendar();
//		cal.setId(1);
//		cal.setSummary("Peter");
	}
	
	public List getEvents(Object calId)
	throws Exception
	{
		return events;
	}

	public List getSelectedCalendars()
	throws Exception
	{
		return Collections.nCopies(1, cal);
	}
	
	public DateInterval getInterval()
	{
		return interval;
	}

	
}
  
}

