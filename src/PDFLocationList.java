import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public class PDFLocationList extends PDFTextStripper {
    public static List<TextPosition> wordPositions = new ArrayList<TextPosition>(); //placeholder variable used to process text
    public PDFLocationList() throws IOException {
    }

    /**
     *
     * @param wordPositions, a list of TextPosition objects
     * @return A list of lines, which are lists of BetterTextPosition objects
     */
    public static List<List<BetterTextPosition>> PositionArrSplitter(List<TextPosition> wordPositions, boolean isTwoColumn) {
        List<List<BetterTextPosition>> output = new ArrayList<>();
        while(Character.isWhitespace(wordPositions.get(0).getUnicode().charAt(0)))
        {
            wordPositions.remove(0);
        }

        String currWord = wordPositions.get(0).getUnicode();
        double startXPosition = wordPositions.get(0).getXDirAdj();
        double currHeight = wordPositions.get(0).getHeight();
        double currTotalWidth = wordPositions.get(0).getWidth();
        double currXPosition = startXPosition + currTotalWidth;
        double currYPosition = wordPositions.get(0).getYDirAdj();
        double currEndYPosition = wordPositions.get(0).getEndY();
        List<BetterTextPosition> line = new ArrayList<>();
        output.add(line);
        //right side of column, only used if isTwoColumn
        List<List<BetterTextPosition>> rightColumn = new ArrayList<>();
        List<BetterTextPosition> rLine = new ArrayList<>();
        rightColumn.add(rLine);
        int index = 0;
        int rIndex = 0;
        for (int i = 1; i < wordPositions.size(); i++) {
            if (Character.isWhitespace(wordPositions.get(i).getUnicode().charAt(0)))
                continue;
            if (wordPositions.get(i).getXDirAdj() > currXPosition + 1 ||
                    wordPositions.get(i).getXDirAdj() < currXPosition - 1) {
                //create new word
                BetterTextPosition newWord = new BetterTextPosition(currWord, startXPosition, currYPosition, currEndYPosition, currHeight, currTotalWidth);
                currWord = wordPositions.get(i).getUnicode();
                startXPosition = wordPositions.get(i).getXDirAdj();
                currHeight = wordPositions.get(i).getHeight();
                currTotalWidth = wordPositions.get(i).getWidth();
                currXPosition = startXPosition + currTotalWidth;
                currYPosition = wordPositions.get(i).getYDirAdj();
                currEndYPosition = wordPositions.get(i).getEndY();
                if(isTwoColumn && newWord.getXDirAdj() > 310) {
                    rightColumn.get(rIndex).add(newWord);
                }
                else {
                    output.get(index).add(newWord);
                }
                if(rightColumn.get(rIndex).size() > 0 && isTwoColumn && startXPosition > 310 && currYPosition != rightColumn.get(rIndex).get(rightColumn.get(rIndex).size()-1).getYDirAdj()) {
                    rIndex++;
                    List<BetterTextPosition> newLine = new ArrayList<>();
                    rightColumn.add(newLine);
                }
                else if (output.get(index).size() > 0 && currYPosition != output.get(index).get(output.get(index).size()-1).getYDirAdj() && (startXPosition < 300 || !isTwoColumn)) {
                    index++;
                    List<BetterTextPosition> newLine = new ArrayList<>();
                    output.add(newLine);
                }

            } else {
                currXPosition += wordPositions.get(i).getWidth();
                currTotalWidth += wordPositions.get(i).getWidth();
                currWord += wordPositions.get(i).getUnicode();
            }
        }
        if(isTwoColumn)
        {
            for(int i = 0; i < rightColumn.size(); i++)
            {
                output.add(rightColumn.get(i));
            }
        }
        return output;

    }

    /**
     *
     * @param pdfPath
     * @throws IOException
     */
    private static void pdfToImages(String pdfPath, String targetDir, String testName) throws IOException{
        PDDocument document = null;
        int questionNumber = 1;
        //iterate through each page
        //load list with pdf data
        double DPISCALE = 300.0/72;
        try {
            document = PDDocument.load(new File(pdfPath));
            PDFTextStripper stripper = new PDFLocationList();
            stripper.setSortByPosition( true );
            boolean isTwoColumn = isTwoColumn(pdfPath);
            for(int page = 0; page < document.getNumberOfPages(); page++) {
                wordPositions.clear();
                stripper.setStartPage(page+1);
                stripper.setEndPage(page+1);
                Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
                stripper.writeText(document, dummy);
                if(wordPositions.size() == 0)
                {
                    continue;
                }
                List<List<BetterTextPosition>> betterWordPositions = PositionArrSplitter(wordPositions, isTwoColumn);
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                new File(targetDir + "\\" + testName).mkdirs();
                String DIR = targetDir + "\\" + testName + "\\";
                BufferedImage renderedPage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                double currentTop = 0;
                double currentLeft = 0;
                double currentBottom = 0;
                double currentRight = renderedPage.getWidth()/DPISCALE;
                if(isTwoColumn)
                {
                    currentRight = renderedPage.getWidth()/DPISCALE/2;
                }
                boolean questionOneFound = false;
                /* Debugging test
                if(page == 0)
                {
                    BufferedImage test = new BufferedImage((int) (40.21728 * DPISCALE), (int) (7.247768 * DPISCALE), BufferedImage.TYPE_INT_RGB);
                    test.getGraphics().drawImage(renderedPage, 0, 0, (int) (40.21728 * DPISCALE), (int) (7.247768 * DPISCALE), (int) (54 * DPISCALE),
                            (int) ((59.52002 - 7.24776) * DPISCALE), (int) (94.21728 * DPISCALE), (int) ((59.52002) * DPISCALE), null);
                    String filename = DIR + "test-" + questionNumber + ".png";
                    ImageIOUtil.writeImage(test, filename, 300);
                }
                 */
                /*for(int i = 0; i < betterWordPositions.size(); i++) {
                    for(int j = 0; j < betterWordPositions.get(i).size(); j++)
                    {
                        System.out.print(betterWordPositions.get(i).get(j).getContent() + " ");
                    }
                    System.out.println();
                }
                */
                //Scanning each line for question number
                for(int line = 0; line < betterWordPositions.size(); line++) {
                    //Ignoring empty lines
                    if(betterWordPositions.get(line).size() == 0 && line != betterWordPositions.size()-1)
                    {
                    }
                    //Getting the question screenshot once the scan reaches the bottom of the page
                    else if(line == betterWordPositions.size()-1)
                    {
                        currentBottom = renderedPage.getHeight()/DPISCALE - 36;
                        int width = (int) ((currentRight - currentLeft)*DPISCALE)+20;
                        int height = (int) ((currentBottom - currentTop)*DPISCALE)+50;
                        BufferedImage croppedQuestion = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                        croppedQuestion.getGraphics().drawImage(renderedPage, 0, 0, width, height, (int) (currentLeft*DPISCALE)-20,
                                (int) (currentTop*DPISCALE)-50, (int) (currentRight*DPISCALE), (int) (currentBottom*DPISCALE), null);
                        String fileName = DIR + "question-" + questionNumber + ".png";
                        ImageIOUtil.writeImage(croppedQuestion, fileName, 300);
                        questionNumber++;
                        currentTop = currentBottom;
                    }
                    //Dealing with the first question of the page
                    else if((betterWordPositions.get(line).get(0).getContent().substring(0, 1).equals("" + (questionNumber)) ||
                            (betterWordPositions.get(line).get(0).getContent().length() > 1 && betterWordPositions.get(line).get(0).getContent().substring(0, 2).equals("" + (questionNumber))))
                            && !questionOneFound && betterWordPositions.get(line).get(0).getXDirAdj() < 100)
                    {
                        currentTop = betterWordPositions.get(line).get(0).getYDirAdj() - betterWordPositions.get(line).get(0).getHeight();
                        /* Potential screenshot fix due to inaccurate coordinates
                        boolean allWhite = false;
                        while(!allWhite)
                        {
                            //iterate through the current y level, with a width of 2*width, from xpos - width, to xpos + width
                            //if allwhite, end loop. Otherwise, decrement y level
                            int charLeftSide = (int) (betterWordPositions.get(line).get(0).getXDirAdj()*DPISCALE);
                            int charRightSide = (int) ((betterWordPositions.get(line).get(0).getXDirAdj() + 2*betterWordPositions.get(line).get(0).getWidth())*DPISCALE);
                            allWhite = true;
                            for(int x = charLeftSide; x < charRightSide; x++)
                            {
                                if(renderedPage.getRGB(x, (int)currentTop) != Color.WHITE.getRGB()) {
                                    allWhite = false;
                                    break;
                                }
                            }
                            currentTop--;
                        }
                        currentTop -= 5; //giving extra whitespace
                        */
                        currentLeft = betterWordPositions.get(line).get(0).getXDirAdj();
                        currentBottom = currentTop + betterWordPositions.get(line).get(0).getHeight(); //giving extra whitespace

                        //currentRight = (int) betterWordPositions.get(line).get(betterWordPositions.get(0).size()-1).getXDirAdj() +
                                //(int) betterWordPositions.get(0).get(betterWordPositions.get(0).size()-1).getWidth();
                        questionOneFound = true;
                    }
                    //Screenshotting question #: questionNumber once finding the line with questionNumber + 1
                    else if(questionOneFound && betterWordPositions.get(line).get(0).getContent().substring(0, 1).equals("" + (questionNumber+1)) ||
                            (betterWordPositions.get(line).get(0).getContent().length() > 1 && betterWordPositions.get(line).get(0).getContent().substring(0, 2).equals("" + (questionNumber+1))) )
                    {
                        currentBottom = betterWordPositions.get(line).get(0).getYDirAdj()-betterWordPositions.get(line).get(0).getHeight();
                        int width = (int) ((currentRight - currentLeft)*DPISCALE)+20;
                        int height;
                        double tempCurrentBottom = currentBottom;
                        if(currentBottom > currentTop) {
                            height = (int) ((currentBottom - currentTop) * DPISCALE) + 50;
                            if(Math.abs(betterWordPositions.get(line).get(0).getXDirAdj()-currentLeft) > 12){
                                continue;
                            }
                        }
                        else {
                            currentBottom = renderedPage.getHeight()/DPISCALE - 36;
                            height = (int) ((currentBottom - currentTop)*DPISCALE)+50;
                        }
                        BufferedImage croppedQuestion = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                        croppedQuestion.getGraphics().drawImage(renderedPage, 0, 0, width, height, (int) (currentLeft*DPISCALE)-20,
                                (int) (currentTop*DPISCALE) - 50, (int) (currentRight*DPISCALE), (int) (currentBottom*DPISCALE), null);
                        String fileName = DIR + "question-" + questionNumber + ".png";
                        ImageIOUtil.writeImage(croppedQuestion, fileName, 300);
                        questionNumber++;
                        currentBottom = tempCurrentBottom;
                        if(currentBottom < currentTop)
                        {
                            currentRight = renderedPage.getWidth()/DPISCALE;
                        }
                        currentTop = currentBottom;
                        currentLeft = betterWordPositions.get(line).get(0).getXDirAdj();
                    }
                    //update all appropriate variables for the line
                    else
                    {

                    }
                }
            }
        }
        finally {
            if( document != null ) {
                document.close();
            }
        }
    }

    public static boolean isTwoColumn(String pdfPath) throws IOException{
        PDDocument document = null;
        int questionNumber = 1;
        //iterate through each page
        //load list with pdf data
        double DPISCALE = 300.0/72;
        boolean isTwoColumn = false;
        try {
            document = PDDocument.load(new File(pdfPath));
            PDFTextStripper stripper = new PDFLocationList();
            stripper.setSortByPosition( true );
            for(int page = 0; page < 2; page++) {
                wordPositions.clear();
                stripper.setStartPage(page+1);
                stripper.setEndPage(page+1);

                Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
                stripper.writeText(document, dummy);
                List<List<BetterTextPosition>> betterWordPositions = PositionArrSplitter(wordPositions, isTwoColumn);
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                BufferedImage renderedPage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                double currentTop = 0;
                double currentLeft = 0;
                double currentBottom = 0;
                double currentRight = renderedPage.getWidth()/DPISCALE;
                boolean questionOneFound = false;
                //Scanning each line for question number
                for(int line = 0; line < betterWordPositions.size(); line++) {
                    //Ignoring empty lines
                    if(betterWordPositions.get(line).size() == 0 && line != betterWordPositions.size()-1)
                    {
                    }
                    //Getting the question screenshot once the scan reaches the bottom of the page
                    else if(line == betterWordPositions.size()-1)
                    {
                        currentBottom = renderedPage.getHeight()/DPISCALE - 36;
                        int width = (int) ((currentRight - currentLeft)*DPISCALE)+20;
                        int height = (int) ((currentBottom - currentTop)*DPISCALE)+40;
                        questionNumber++;
                        currentTop = currentBottom;
                    }
                    //Dealing with the first question of the page
                    else if((betterWordPositions.get(line).get(0).getContent().substring(0, 1).equals("" + (questionNumber)) ||
                            (betterWordPositions.get(line).get(0).getContent().length() > 1 && betterWordPositions.get(line).get(0).getContent().substring(0, 2).equals("" + (questionNumber))))
                            && !questionOneFound && betterWordPositions.get(line).get(0).getXDirAdj() < 100)
                    {
                        currentTop = betterWordPositions.get(line).get(0).getYDirAdj() - betterWordPositions.get(line).get(0).getHeight();

                        currentLeft = betterWordPositions.get(line).get(0).getXDirAdj();
                        currentBottom = currentTop + betterWordPositions.get(line).get(0).getHeight(); //giving extra whitespace

                        //currentRight = (int) betterWordPositions.get(line).get(betterWordPositions.get(0).size()-1).getXDirAdj() +
                        //(int) betterWordPositions.get(0).get(betterWordPositions.get(0).size()-1).getWidth();
                        questionOneFound = true;
                    }
                    //Screenshotting question #: questionNumber once finding the line with questionNumber + 1
                    else if(questionOneFound && betterWordPositions.get(line).get(0).getContent().substring(0, 1).equals("" + (questionNumber+1)) ||
                            (betterWordPositions.get(line).get(0).getContent().length() > 1 && betterWordPositions.get(line).get(0).getContent().substring(0, 2).equals("" + (questionNumber+1))) )
                    {
                        currentBottom = betterWordPositions.get(line).get(0).getYDirAdj()-betterWordPositions.get(line).get(0).getHeight();
                        int width = (int) ((currentRight - currentLeft)*DPISCALE)+20;
                        int height;
                        double tempCurrentBottom = currentBottom;
                        if(currentBottom > currentTop) {
                            height = (int) ((currentBottom - currentTop) * DPISCALE) + 40;
                            if(Math.abs(betterWordPositions.get(line).get(0).getXDirAdj()-currentLeft) > 12){
                                continue;
                            }
                        }
                        else {
                            currentBottom = renderedPage.getHeight()/DPISCALE - 36;
                            height = (int) ((currentBottom - currentTop)*DPISCALE)+40;
                        }
                        questionNumber++;
                        currentBottom = tempCurrentBottom;
                        if(currentBottom < currentTop)
                        {
                            currentRight = renderedPage.getWidth()/DPISCALE;
                        }
                        currentTop = currentBottom;
                        currentLeft = betterWordPositions.get(line).get(0).getXDirAdj();
                    }
                    //update all appropriate variables for the line
                    else
                    {

                    }
                }
                isTwoColumn = !questionOneFound;
            }
        }
        finally {
            if( document != null ) {
                document.close();
            }
        }
        return isTwoColumn;
    }
    /**
     * @throws IOException If there is an error parsing the document.
     */
    public static void main( String[] args ) throws IOException {
        File testLocations = new File("C:\\Users\\aquat\\Downloads\\FAMAT Test List Copy - 000final.csv");
        Scanner inFile = new Scanner(testLocations);
        HashMap<String, String[]> testRenaming = new HashMap<>();
        HashSet<String> existingNames = new HashSet<>();
        while(inFile.hasNextLine())
        {
            String currLine = inFile.nextLine();
            String[] testInfo = currLine.split(",");
            if(testInfo.length > 10 && testInfo[0].length() > 0 && Character.isDigit(testInfo[0].charAt(0)))
            {
                String key = testInfo[8];
                if(key.substring(key.length()-3).equals("pdf"))
                {
                    existingNames.add(key);
                    testRenaming.put(key, testInfo);
                }
            }
        }
        String testDir = "C:\\Users\\aquat\\Downloads\\tests\\tests\\";
        for(String s: testRenaming.keySet())
        {
            String[] testInfo = testRenaming.get(s);
            try {
                System.out.println("Trying " + testInfo[0]);
                int iD = Integer.parseInt(testInfo[0]);
                if(iD > 9000 && testInfo[1].equals("Test") && iD != 9722 && iD != 9457 && iD != 10262 && iD != 9461)
                {
                    File testFile = new File(testDir + s);
                    String targetDir = "C:\\Users\\aquat\\Downloads\\pdfstuff";
                    String category = testInfo[5];
                    if(category.equals("Precalculus/Alpha"))
                    {
                        category = "Alpha";
                    }
                    if(category.equals("Calculus/Mu"))
                    {
                        category = "Mu";
                    }
                    if(category.equals("Algebra I"))
                    {
                        targetDir += "\\algebra1";
                        if(!testInfo[6].equals("(Individual)"))
                        {
                            continue;
                        }
                    }
                    else if(category.equals("Algebra II"))
                    {
                        targetDir += "\\algebra2";
                        if(!testInfo[6].equals("(Individual)"))
                        {
                            continue;
                        }
                    }
                    else if(category.equals("Geometry"))
                    {
                        targetDir += "\\geometry";
                        if(!testInfo[6].equals("(Individual)"))
                        {
                            continue;
                        }
                    }
                    else if(category.equals("Theta"))
                    {
                        targetDir += "\\theta";
                        if(testInfo[6].equals("Geometry"))
                        {
                            targetDir += "\\geometry";
                        }
                        else if(testInfo[6].equals("Functions"))
                        {
                            targetDir += "\\functions";
                        }
                        else if(testInfo[6].equals("Logs and Exponents"))
                        {
                            targetDir += "\\logsandexponents";
                        }
                        else if(testInfo[6].equals("Applications"))
                        {
                            targetDir += "\\applications";
                        }
                        else if(testInfo[6].equals("\"Circumference, Perimeter, Area, and Volume\""))
                        {
                            targetDir += "\\cpav";
                        }
                        else if(testInfo[6].equals("Equations and Inequalities"))
                        {
                            targetDir += "\\equationsandinequalities";
                        }
                        else if(testInfo[6].equals("Quadrilaterals"))
                        {
                            targetDir += "\\quadrilaterals";
                        }
                        else if(!testInfo[6].equals("(Individual)"))
                        {
                            continue;
                        }
                    }
                    else if(category.equals("Alpha"))
                    {
                        targetDir += "\\alpha";
                        if(testInfo[6].equals("Matrices and Vectors"))
                        {
                            targetDir += "\\matricesandvectors";
                        }
                        else if(testInfo[6].equals("Complex Numbers"))
                        {
                            targetDir += "\\complexnumbers";
                        }
                        else if(testInfo[6].equals("Trigonometry"))
                        {
                            targetDir += "\\trigonometry";
                        }
                        else if(testInfo[6].equals("Applications"))
                        {
                            targetDir += "\\applications";
                        }
                        else if(testInfo[6].equals("Analytic Geometry"))
                        {
                            targetDir += "\\analyticgeometry";
                        }
                        else if(testInfo[6].equals("Equations and Inequalities"))
                        {
                            targetDir += "\\equationsandinequalities";
                        }
                        else if(!testInfo[6].equals("(Individual)"))
                        {
                            continue;
                        }
                    }
                    else if(category.equals("Mu"))
                    {
                        targetDir += "\\mu";
                        if(testInfo[6].equals("Integration"))
                        {
                            targetDir += "\\integration";
                        }
                        else if(testInfo[6].equals("Limits and Derivatives"))
                        {
                            targetDir += "\\limitsandderivatives";
                        }
                        else if(testInfo[6].equals("Area and Volume"))
                        {
                            targetDir += "\\areaandvolume";
                        }
                        else if(testInfo[6].equals("Applications"))
                        {
                            targetDir += "\\applications";
                        }
                        else if(testInfo[6].equals("BC Calculus"))
                        {
                            targetDir += "\\bccalculus";
                        }
                        else if(testInfo[6].equals("Sequences and Series"))
                        {
                            targetDir += "\\sequencesandseries";
                        }
                        else if(!testInfo[6].equals("(Individual)"))
                        {
                            continue;
                        }
                    }
                    else if(category.equals("Statistics"))
                    {
                        targetDir += "\\statistics";
                        if(!testInfo[6].equals("(Individual)"))
                        {
                            continue;
                        }
                    }
                    else {
                        continue;
                    }
                    String testName = testInfo[0];
                    String testFolderName = targetDir + "\\" + testName;
                    File testFolder = new File(testFolderName);
                    if(testFolder.exists() && testFolder.isDirectory())
                    {
                        System.out.println(testName + " exists");
                        continue;
                    }
                    pdfToImages(testDir+s, targetDir, testName);
                    System.out.println("Converted " + testName);
                }
            }
            finally {
                ;
            }
        }
    }

    /**
     * Override the default functionality of PDFTextStripper.writeString()
     */
    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        for (TextPosition text : textPositions) {
            /*System.out.println(text.getUnicode()+ " [(X=" + text.getXDirAdj() + ",Y=" +
                    text.getYDirAdj() + ") height=" +  text.getHeightDir() + " width=" +
                    text.getWidthDirAdj() + "]");

*/


            wordPositions.add(text);
        }
    }
}