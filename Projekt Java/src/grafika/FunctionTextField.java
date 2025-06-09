package grafika;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class FunctionTextField extends JTextField{
	//istnieje, aby kiedy się zmienia ustawienia jak ma być wypisywanafunkcja, to zmienia się tylko jeśli jest "upToDate"
	
	//opcja dodania listenera wyłapującego tylko zmiany dokonane przez użytkownika
	private int doneByProgramFlag = 0;
	//zapamiętuje czy ostatnia zmiana została wykonana przez użytkownika czy przez program
	public boolean isUpToDate;
	
	//będzie zmieniał connected jak sam się zmieni (jeśli jest upToDate)
	private JTextField connected;
	
	public FunctionTextField(String string) {
		super(string);
		isUpToDate = true;
		getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				if(doneByProgramFlag>0) {
					doneByProgramFlag--;
					return;
				}
				isUpToDate = false;

			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {

				if(doneByProgramFlag>0) {
					doneByProgramFlag--;
					return;
				}
				isUpToDate = false;

			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				if(doneByProgramFlag>0) {
					doneByProgramFlag--;
					return;
				}
				isUpToDate = false;

			}
		});

	}
	
	public void setConnected(JTextField t) {
		connected = t;
	}
	
	@Override
	public void setText(String t) {
		doneByProgramFlag += 2;
		isUpToDate = true;
		super.setText(t);
		if(connected != null)
			connected.setText(t);
	}
}