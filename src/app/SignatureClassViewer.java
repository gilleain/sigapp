package app;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.signature.MoleculeSignature;
import org.openscience.cdk.signature.Orbit;

public class SignatureClassViewer extends JFrame 
                                  implements ActionListener, 
                                             ListSelectionListener {
    
    private MoleculePanel moleculePanel;
    
    private TreeThumbViewer treeThumbViewer;
    
    private Map<String, Orbit> orbitMap;
    
    private AtomSymmetryClassGenerator atomSymmetryClassGenerator;
    
    private JButton loadButton;
    
    private JButton showNumbersButton;
    
    public SignatureClassViewer(String[] args) {
        setLayout(new BorderLayout());
        
        List<IGenerator<IAtomContainer>> initialGenerators = 
            new ArrayList<IGenerator<IAtomContainer>>();
        atomSymmetryClassGenerator = new AtomSymmetryClassGenerator();
        initialGenerators.add(atomSymmetryClassGenerator);
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        loadButton = new JButton("Load Molecule");
        loadButton.setActionCommand("LOAD");
        loadButton.addActionListener(this);
        buttonPanel.add(loadButton);
        
        showNumbersButton = new JButton("Toggle Numbers");
        showNumbersButton.setActionCommand("TOGNUM");
        showNumbersButton.addActionListener(this);
        buttonPanel.add(showNumbersButton);
        
        leftPanel.add(buttonPanel, BorderLayout.NORTH);
        
        moleculePanel = new MoleculePanel(700, 700, initialGenerators);
        leftPanel.add(moleculePanel, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.CENTER);
        
        treeThumbViewer = new TreeThumbViewer(700, 700);
        add(treeThumbViewer, BorderLayout.EAST);
        treeThumbViewer.addSelectionListener(this);
        
        orbitMap = new HashMap<String, Orbit>();
        
        String filename;
        if (args.length != 0) {
            filename = args[0];
            loadFile(filename);
        }
        pack();
        setVisible(true);
    }
    
    public void loadFile(File file) {
        try {
            ISimpleChemObjectReader reader =
                new MDLReader(new FileReader(file));
            if (reader == null) return;
            IMolecule molecule = reader.read(new Molecule());
            System.out.println("read");
            moleculePanel.setMoleculeWithoutLayout(molecule);
            for (int i = 0; i < molecule.getAtomCount(); i++) {
                List<IAtom> connected = 
                    molecule.getConnectedAtomsList(molecule.getAtom(i));
                System.out.print(connected.size() + " " + (i + 1) + " ");
                for (IAtom a : connected) {
                    int j = molecule.getAtomNumber(a) + 1;
                    System.out.print(j + ",");
                }
                System.out.println();
            }
            makeSignatures(molecule);
            
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CDKException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void loadFile(String filename)  {
        loadFile(new File(filename));
    }
    
    public void makeSignatures(IMolecule molecule) {
        MoleculeSignature molSig = new MoleculeSignature(molecule);
        List<Orbit> orbits = molSig.calculateOrbits();
        orbitMap.clear();
        treeThumbViewer.clear();
        for (Orbit o : orbits) {
            String sig = o.getLabel();
            treeThumbViewer.addSignature(sig);
            orbitMap.put(sig, o);
        }
    }
    
    public void displaySelectedOrbits(List<Orbit> orbits) {
        atomSymmetryClassGenerator.setOrbits(orbits);
        repaint();
    }
    
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("LOAD")) {
            JFileChooser fileChooser = new JFileChooser(".");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                loadFile(fileChooser.getSelectedFile());
            }
        } else if (e.getActionCommand().equals("TOGNUM")) {
            moleculePanel.toggleShowNumbers();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        List<String> selectedList = treeThumbViewer.getSelected();
        List<Orbit> selectedOrbits = new ArrayList<Orbit>();
        for (String selected : selectedList) {
            System.out.println("selected " + orbitMap.get(selected));
            selectedOrbits.add(orbitMap.get(selected));
        }
        displaySelectedOrbits(selectedOrbits);
    }

    public static void main(String[] args) {
        new SignatureClassViewer(args);
    }

}
