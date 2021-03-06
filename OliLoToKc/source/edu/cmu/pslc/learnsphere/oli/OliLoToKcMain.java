/*
 * Carnegie Mellon University, Human-Computer Interaction Institute
 * Copyright 2015
 * All Rights Reserved
 *
 * Java wrapper for a component that converts an OLI skill model to a KC model file
 * -Peter
 */

package edu.cmu.pslc.learnsphere.oli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.io.CharArrayWriter;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import java.util.regex.Pattern;

import edu.cmu.pslc.datashop.servlet.workflows.WorkflowHelper;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.workflows.AbstractComponent;

public class OliLoToKcMain extends AbstractComponent {


	/**
	 * Main method.
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		OliLoToKcMain tool = new OliLoToKcMain();

		tool.startComponent(args);
	}

	/**
	 * This class runs the LearningCurveVisualization one or more times
	 * depending on the number of input elements.
	 */
	public OliLoToKcMain() {

		super();

	}

	@Override
	protected void runComponent() {

		File outputDirectory = this.runExternal();

		if (outputDirectory.isDirectory() && outputDirectory.canRead()) {
			String outputFileName = getOutputFileName(outputDirectory);
			logger.debug("Output file path: " + outputDirectory.getAbsolutePath() + File.separator + outputFileName);

			File file0 = new File(outputDirectory.getAbsolutePath() + File.separator + outputFileName);


			if (file0 != null && file0.exists() ) {

				Integer nodeIndex0 = 0;
				Integer fileIndex0 = 0;
				String label0 = "tab-delimited";
				this.addOutputFile(file0, nodeIndex0, fileIndex0, label0);

			} else {
				errorMessages.add("cannot add output files");
			}
		} else {
			errorMessages.add("issue with output directory");
		}

		String outputPath = outputDirectory.getAbsolutePath() + "/";

		cleanUpOutputDir(outputDirectory);

		System.out.println(this.getOutput());

	}

	/**
	 * The component program creates a temporary database file and uses a few other temporary
	 * files.  Ensure that these are deleted, but leave the output file (ends in -KCM.txt)
	 */
	private void cleanUpOutputDir(File outputDir) {
		File [] filesInOutputDir = outputDir.listFiles();

		// Loop through files and dirs in output directory
		for (File fileInOutputDir : filesInOutputDir) {
			if (fileInOutputDir != null && fileInOutputDir.exists()) {
				String fileName = fileInOutputDir.getName();
				if (!fileName.contains("-KCM")) {
					// The file or directory is not the output kc model, so delete it.
					try {
						if (!fileInOutputDir.delete()) {
							logger.debug(
							    "Unable to delete this temporary file in the output directory: " + fileName);
						}
					} catch (SecurityException e) {
						logger.debug("Unable to delete " + fileName + 
							". Security Exception: " + e.toString());
					}
				}
			}
		}
	}

	private String getOutputFileName(File outputDir) {
		File [] filesInOutputDir = outputDir.listFiles();

		for (File fileInOutputDir : filesInOutputDir) {
			if (fileInOutputDir != null && fileInOutputDir.exists()) {
				String fileName = fileInOutputDir.getName();
				if (fileName.contains("-KCM")) {
					return fileName;
				}
			}
		}
		logger.error("Could not find the output file by name or it doesn't exist.");
		return "";
	}

	/**
	 * The test() method is used to test the known inputs prior to running.
	 * @return true if passing, false otherwise
	 */
	@Override
	protected Boolean test() {
		Boolean passing = true;


		return passing;
	}

	/**
	 * Parse the options list.
	 */
	@Override
	protected void parseOptions() {
		logger.info("Parsing options.");
	}

	@Override
	protected void processOptions() {
		// addMetaDataFromInput(String fileType, Integer inputNodeIndex, Integer outputNodeIndex, String name)
		Integer outNodeIndex0 = 0;
	}

}

