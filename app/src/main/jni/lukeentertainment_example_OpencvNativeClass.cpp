#include <lukeentertainment_example_OpencvNativeClass.h>
const int MIN_CONTOUR_AREA = 5;

const int RESIZED_IMAGE_WIDTH = 20;
const int RESIZED_IMAGE_HEIGHT = 30;
std::string str="";
std::string strFinalString="";

  class ContourWithData {
  public:
      // member variables ///////////////////////////////////////////////////////////////////////////
      std::vector<cv::Point> ptContour;           // contour
      cv::Rect boundingRect;                      // bounding rect for contour
      float fltArea;                              // area of contour

      ///////////////////////////////////////////////////////////////////////////////////////////////
      bool checkIfContourIsValid() {                              // obviously in a production grade program
          if (fltArea < MIN_CONTOUR_AREA) return false;           // we would have a much more robust function for
          return true;                                            // identifying if a contour is valid !!
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////
      static bool sortByBoundingRectXPosition(const ContourWithData& cwdLeft, const ContourWithData& cwdRight) {      // this function allows us to sort
          return(cwdLeft.boundingRect.x < cwdRight.boundingRect.x);                                                   // the contours from left to right
      }
      static bool sortByBoundingRectYPosition(const ContourWithData& cwdTop, const ContourWithData& cwdBot) {      // this function allows us to sort
           return(cwdTop.boundingRect.y < cwdBot.boundingRect.y);                                                   // the contours from left to right
      }

  };
    int checkBounds(Rect p,Rect c)
    {
        if (   (c.x+c.width) <= (p.x+p.width)&& (c.x) >= (p.x)&& (c.y) >= (p.y)&& (c.y+c.height) <= (p.y+p.height))
            return 1;
        else
            return 0;
    }

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_train
  (JNIEnv *env, jclass, jlong addrRgba,jstring path,jint option,jint lastPos){
 cv::Mat& mRgb=*(cv::Mat*)addrRgba;

    const jsize len = env->GetStringUTFLength(path);
    const char* strChars = env->GetStringUTFChars(path, (jboolean *)0);
    std::string Result(strChars, len);
    char classPath[100],imagePath[100];

    strcpy(classPath,strChars);
    strcpy(imagePath,strChars);

    env->ReleaseStringUTFChars(path, strChars);
    strcat(classPath,"classifications.xml");
    strcat(imagePath,"images.xml");
    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG"," %s\n",classPath);

    std::vector<ContourWithData> allContoursWithData;           // declare empty vectors,
    std::vector<ContourWithData> validContoursWithData,validContours,validContoursWithDataSpe,validContoursWithDataLine;

      cv::Mat imgTrainingNumbers;
      cv::Mat imgGrayscale;
      cv::Mat imgThresh;
      cv::Mat imgThreshCopy;
      cv::Mat lineGrayscale;
      cv::Mat lineThresh;
      cv::Mat lineThreshCopy;
      cv::Mat speThresh;
      cv::Mat speThreshCopy;


    std:: vector<std::vector<cv::Point> > ptContours,ptContoursLine,ptContoursSpe;
    std::vector<cv::Vec4i> v4iHierarchy,v4iHierarchyLine,v4iHierarchySpe;
    cv::Mat matClassificationInts;      // these are our training classifications, note we will have to perform some conversions before writing to file later
    cv::Mat matTrainingImages;
    imgTrainingNumbers =(cv::Mat) mRgb;

    cv::cvtColor(imgTrainingNumbers, imgGrayscale, CV_RGB2GRAY);

    cv::threshold(imgGrayscale,imgThresh, 0, 255, CV_THRESH_BINARY_INV | CV_THRESH_OTSU);
    lineThresh=imgThresh.clone();
    speThresh=imgThresh.clone();


    imgThreshCopy = imgThresh.clone();
    int totalWidth=0;
    cv::findContours(imgThreshCopy,ptContours,v4iHierarchy,cv::RETR_EXTERNAL,cv::CHAIN_APPROX_SIMPLE);
      for (int i = 0; i < ptContours.size(); i++) {               // for each contour
                    ContourWithData contourWithData;                                                    // instantiate a contour with data object
                    contourWithData.ptContour = ptContours[i];                                          // assign contour to contour with data
                    contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);         // get the bounding rect
                    contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);               // calculate the contour area
                    if(contourWithData.fltArea >MIN_CONTOUR_AREA){
                         validContours.push_back(contourWithData);
                         totalWidth+=contourWithData.fltArea;
                              }                             // add contour with data object to list of all contours with data
      }

     int size=validContours.size();
     int dilationValue=(float(totalWidth/size))*0.04;
     Mat elementSpe = Mat::ones(dilationValue,1, CV_8UC1);
         cv::dilate(speThresh,speThresh, elementSpe);
         speThreshCopy=speThresh.clone();
         mRgb=speThresh;
         cv::findContours(speThreshCopy,ptContoursSpe,v4iHierarchySpe,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);

         for (int i = 0; i < ptContoursSpe.size(); i++) {
                             ContourWithData contourWithData;
                             contourWithData.ptContour = ptContoursSpe[i];
                             contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);
                             contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);
                             if(contourWithData.fltArea >MIN_CONTOUR_AREA*dilationValue)
                                 validContoursWithDataSpe.push_back(contourWithData);
             }

         if(validContoursWithDataSpe.size()!=validContours.size())
         {
                 for(int i=0;i<validContoursWithDataSpe.size();i++)
                 {
                     std::vector<ContourWithData> validSpe;

                    for(int j=0;j<validContours.size();j++)
                    {
                         if(checkBounds(validContoursWithDataSpe[i].boundingRect,validContours[j].boundingRect)){

                             validSpe.push_back(validContours[j]);
                         }
                    }
                 int mx=validSpe[0].boundingRect.x,my=validSpe[0].boundingRect.y;
                 int mw=validSpe[0].boundingRect.width+mx,mh=validSpe[0].boundingRect.height+my;
                 if(validSpe.size()>1){
                     mx=min(mx,validSpe[1].boundingRect.x);
                     my=min(my,validSpe[1].boundingRect.y);
                     mw=max(mw,validSpe[1].boundingRect.width+mx);
                     mh=max(mh,validSpe[1].boundingRect.height+my);
                 }

                         ContourWithData contourWithData;
                         contourWithData.ptContour=validSpe[0].ptContour;
                         contourWithData.boundingRect = Rect(Point(mx,my),Point(mw,mh));

                         validContoursWithData.push_back(contourWithData);
                 }
         }else
         {
             validContoursWithData=validContours;
         }
      Mat element = Mat::ones(1,50, CV_8UC1);

      cv::dilate(lineThresh,lineThresh, element);
      lineThresh+=speThresh;
      lineThreshCopy = lineThresh.clone();
     cv::findContours(lineThreshCopy,ptContoursLine,v4iHierarchyLine,cv::RETR_EXTERNAL,cv::CHAIN_APPROX_SIMPLE);
     for (int i = 0; i < ptContoursLine.size(); i++) {               // for each contour
                   ContourWithData contourWithData;                                                    // instantiate a contour with data object
                   contourWithData.ptContour = ptContoursLine[i];                                          // assign contour to contour with data
                   contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);         // get the bounding rect
                   contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);               // calculate the contour area
                   if(contourWithData.fltArea >MIN_CONTOUR_AREA*50)
                        validContoursWithDataLine.push_back(contourWithData);                                   // add contour with data object to list of all contours with data
     }

    int intChar='0';
    int flag=0;
    if(option==1)
    {
              cv::FileStorage fsClassifications(classPath, cv::FileStorage::READ);        // open the classifications file
              fsClassifications["classifications"] >> matClassificationInts;      // read classifications section into Mat classifications variable
              fsClassifications.release();

              cv::FileStorage fsTrainingImages(imagePath, cv::FileStorage::READ);          // open the training images file
              fsTrainingImages["images"] >> matTrainingImages;           // read images section into Mat training images variable
              fsTrainingImages.release();
              intChar=lastPos;
              flag=1;
    }

    std::sort(validContoursWithDataLine.begin(),validContoursWithDataLine.end(), ContourWithData::sortByBoundingRectYPosition);
            for (int i = 0; i < validContoursWithDataLine.size(); i++) {
                         std::vector<ContourWithData> validLetters;
                         cv::Rect boundingRect = validContoursWithDataLine[i].boundingRect;
                         for(int j=0;j<validContoursWithData.size();j++)
                         {
                               if(checkBounds(boundingRect,validContoursWithData[j].boundingRect))
                                    validLetters.push_back(validContoursWithData[j]);

                         }
                         std::sort(validLetters.begin(),validLetters.end(), ContourWithData::sortByBoundingRectXPosition);
                         for(int k=0;k<validLetters.size();k++)
                         {
                              cv::Rect boundingRect = validLetters[k].boundingRect;
                              cv::rectangle(imgTrainingNumbers, boundingRect, cv::Scalar(255,0,0),1);      // draw red rectangle around each contour as we ask user for input

                             cv::Mat matROI = imgThresh(boundingRect);
                             cv::Mat matROIResized;
                             cv::resize(matROI, matROIResized, cv::Size(RESIZED_IMAGE_WIDTH, RESIZED_IMAGE_HEIGHT));     // resize image, this will be more consistent for recognition and storage
                              matClassificationInts.push_back(intChar);

                             cv::Mat matImageFloat;
                             matROIResized.convertTo(matImageFloat, CV_32FC1);
                             cv::Mat matImageReshaped = matImageFloat.reshape(1, 1);
                             matTrainingImages.push_back(matImageReshaped);

                         }
                         intChar++;

            }

          cv::Mat matClassificationFloats;
          matClassificationInts.convertTo(matClassificationFloats,CV_32FC1);
          cv::FileStorage fsClassifications(classPath, cv::FileStorage::WRITE);           // open the classifications file
          fsClassifications << "classifications" <<matClassificationInts;        // write classifications into classifications section of classifications file
          fsClassifications.release();                                            // close the classifications file

          cv::FileStorage fsTrainingImages(imagePath, cv::FileStorage::WRITE);         // open the training images file

          fsTrainingImages << "images" << matTrainingImages;         // write training images into images section of images file
      fsTrainingImages.release();
      mRgb=imgTrainingNumbers;
      return (jint)intChar;
  }


  JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_testInput
    (JNIEnv *env, jclass,jlong addrRgba,jstring path){
cv::Mat& mRgb=*(cv::Mat*)addrRgba;

        const jsize len = env->GetStringUTFLength(path);
       const char* strChars = env->GetStringUTFChars(path, (jboolean *)0);
       std::string Result(strChars, len);
       char classPath[100],imagePath[100],dataPath[100];

       strcpy(classPath,strChars);
       strcpy(imagePath,strChars);
       strcpy(dataPath,strChars);

       env->ReleaseStringUTFChars(path, strChars);
        strcat(classPath,"classifications.xml");
        strcat(imagePath,"images.xml");
        strcat(dataPath,"data.txt");

    std::vector<ContourWithData> validContours,validContoursWithData,validContoursWithDataSpe,validContoursWithDataWord,validContoursWithDataLine;         // we will fill these shortly

    cv::Mat matClassificationFloats;      // we will read the classification numbers into this variable as though it is a vector

    cv::FileStorage fsClassifications(classPath, cv::FileStorage::READ);        // open the classifications file

    fsClassifications["classifications"] >> matClassificationFloats;      // read classifications section into Mat classifications variable
    fsClassifications.release();                                        // close the classifications file

    cv::Mat matTrainingImages;         // we will read multiple images into this single image variable as though it is a vector

    cv::FileStorage fsTrainingImages(imagePath, cv::FileStorage::READ);          // open the training images file

    fsTrainingImages["images"] >> matTrainingImages;           // read images section into Mat training images variable
    fsTrainingImages.release();
                                                // close the traning images file
    cv::KNearest kNearest=cv::KNearest();
    kNearest.train(matTrainingImages,matClassificationFloats);


    cv::Mat testingNumbers =mRgb.clone();
    cv::Mat thresh;
    cv::cvtColor(testingNumbers,thresh, CV_RGB2GRAY);
    mRgb=thresh;

    cv::Mat matThresh =thresh.clone();
    cv::Mat imgThresh =thresh.clone();
    cv::Mat lineThresh =thresh.clone();
    cv::Mat speThresh=thresh.clone();

    if (testingNumbers.empty()) {                                // if unable to open image
         __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error: image not read from file\n\n");         // show error message on command line
        return(0);                                                  // and exit program
    }

    cv::Mat matThreshCopy;

    cv::Mat imgThreshCopy;

    cv::Mat lineThreshCopy;

    cv::Mat speThreshCopy;

    matThreshCopy = matThresh.clone();

    std::vector<std::vector<cv::Point> > ptContours,ptContoursWord,ptContoursLine,ptContoursSpe;
    std::vector<cv::Vec4i> v4iHierarchy,v4iHierarchySpe,v4iHierarchyWord,v4iHierarchyLine;
    cv::findContours(matThreshCopy,ptContours,v4iHierarchy,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);
    int totalWidth=0;
    for (int i = 0; i < ptContours.size(); i++) {               // for each contour
        ContourWithData contourWithData;                                                    // instantiate a contour with data object
        contourWithData.ptContour = ptContours[i];                                          // assign contour to contour with data
        contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);         // get the bounding rect
        contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);               // calculate the contour area
        if(contourWithData.fltArea >MIN_CONTOUR_AREA){
            validContours.push_back(contourWithData);
            totalWidth+=contourWithData.fltArea;
        }
    }
    int size=validContours.size();
    int dilationValue=(float(totalWidth/size))*0.04;
    Mat elementSpe = Mat::ones(dilationValue,1, CV_8UC1);
    cv::dilate(speThresh,speThresh, elementSpe);
    speThreshCopy=speThresh.clone();
    cv::findContours(speThreshCopy,ptContoursSpe,v4iHierarchySpe,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);
    for (int i = 0; i < ptContoursSpe.size(); i++) {
                        ContourWithData contourWithData;
                        contourWithData.ptContour = ptContoursSpe[i];
                        contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);
                        contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);
                        if(contourWithData.fltArea >MIN_CONTOUR_AREA*dilationValue)
                            validContoursWithDataSpe.push_back(contourWithData);
        }

    if(validContoursWithDataSpe.size()!=validContours.size())
    {
            for(int i=0;i<validContoursWithDataSpe.size();i++)
            {
                std::vector<ContourWithData> validSpe;

               for(int j=0;j<validContours.size();j++)
               {
                    if(checkBounds(validContoursWithDataSpe[i].boundingRect,validContours[j].boundingRect)){

                        validSpe.push_back(validContours[j]);
                    }
               }

               int mx=validSpe[0].boundingRect.x,my=validSpe[0].boundingRect.y;
                int mw=validSpe[0].boundingRect.width+mx,mh=validSpe[0].boundingRect.height+my;
                for(int k=1;k<validSpe.size();k++)
                {
                    mx=min(mx,validSpe[k].boundingRect.x);
                    my=min(my,validSpe[k].boundingRect.y);
                    mw=max(mw,validSpe[k].boundingRect.width+mx);
                    mh=max(mh,validSpe[k].boundingRect.height+my);
                }

                        ContourWithData contourWithData;
                        contourWithData.ptContour=validSpe[0].ptContour;
                        contourWithData.boundingRect = Rect(Point(mx,my),Point(mw,mh));

                        validContoursWithData.push_back(contourWithData);

            }
    }else
    {
        validContoursWithData=validContours;
    }

    Mat element = Mat::ones(1,dilationValue, CV_8UC1);
    cv::dilate(imgThresh,imgThresh, element);
    imgThresh+=speThresh;
    imgThreshCopy = imgThresh.clone();
    cv::findContours(imgThreshCopy,ptContoursWord,v4iHierarchyWord,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);
    for (int i = 0; i < ptContoursWord.size(); i++) {
                    ContourWithData contourWithData;
                    contourWithData.ptContour = ptContoursWord[i];
                    contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);
                    contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);
                    if(contourWithData.fltArea >MIN_CONTOUR_AREA*dilationValue)
                        validContoursWithDataWord.push_back(contourWithData);
    }
    Mat kernel = Mat::ones(1,dilationValue*5, CV_8UC1);
    cv::dilate(lineThresh,lineThresh,kernel);
    lineThresh+=speThresh;
    lineThreshCopy = lineThresh.clone();
    cv::findContours(lineThreshCopy,ptContoursLine,v4iHierarchyLine,CV_RETR_EXTERNAL,CV_CHAIN_APPROX_SIMPLE);
    for (int i = 0; i < ptContoursLine.size(); i++) {
                    ContourWithData contourWithData;
                    contourWithData.ptContour = ptContoursLine[i];
                    contourWithData.boundingRect = cv::boundingRect(contourWithData.ptContour);
                    contourWithData.fltArea = cv::contourArea(contourWithData.ptContour);
                    if(contourWithData.fltArea >MIN_CONTOUR_AREA*5*dilationValue)
                         validContoursWithDataLine.push_back(contourWithData);
    }

    std::wofstream out(dataPath,std::ios::out);
    out<<wchar_t(L"");
    out.close();

    //Line and space algorithm

    std::sort(validContoursWithDataLine.begin(),validContoursWithDataLine.end(), ContourWithData::sortByBoundingRectYPosition);
    for(int m=0;m<validContoursWithDataLine.size();m++)
    {

        std::vector<ContourWithData> validLine;
        for(int l=0;l<validContoursWithDataWord.size();l++)
        {
             if(checkBounds(validContoursWithDataLine[m].boundingRect,validContoursWithDataWord[l].boundingRect)){
                    __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","\n\level 1\n");
                  validLine.push_back(validContoursWithDataWord[l]);}

        }
        std::sort(validLine.begin(),validLine.end(), ContourWithData::sortByBoundingRectXPosition);
        for (int i = 0; i < validLine.size(); i++) {
                     std::vector<ContourWithData> validLetters;
                     cv::Rect boundingRect = validLine[i].boundingRect;

                     for(int j=0;j<validContoursWithData.size();j++)
                     {
                           if(checkBounds(boundingRect,validContoursWithData[j].boundingRect)){

                                validLetters.push_back(validContoursWithData[j]);}

                     }

                     std::sort(validLetters.begin(),validLetters.end(), ContourWithData::sortByBoundingRectXPosition);
                     for(int k=0;k<validLetters.size();k++)
                     {
                        cv::rectangle(testingNumbers,validLetters[k].boundingRect,cv::Scalar(0, 255, 0),0);
                        cv::Mat matROI = matThresh(validLetters[k].boundingRect);
                        cv::Mat matROIResized;
                        cv::resize(matROI, matROIResized, cv::Size(RESIZED_IMAGE_WIDTH, RESIZED_IMAGE_HEIGHT));     // resize image, this will be more consistent for recognition and storage
                        cv::Mat matROIFloat;
                        matROIResized.convertTo(matROIFloat, CV_32FC1);             // convert Mat to float, necessary for call to find_nearest
                        float fltCurrentChar =kNearest.find_nearest(matROIFloat.reshape(1,1),1);
                       std::wofstream out(dataPath,std::ios::out|std::ios::app);
                        out<<wchar_t(int(fltCurrentChar));
                        out.close();
                     }

                     std::wofstream out(dataPath,std::ios::out|std::ios::app);
                     out<<wchar_t(32);
                     out.close();
        }
    }
    mRgb=testingNumbers;

    return 1;
    }


JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_trainIndi
    (JNIEnv *env, jclass,jlong addrRgba,jstring path,jint value){

    return 1;
    }

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_processImage
    (JNIEnv *env, jclass,jlong addrRgba){
     cv::Mat& mRgb=*(cv::Mat*)addrRgba;

     cv::Mat matTestingNumbers =mRgb;           // read in the test numbers image

     if (matTestingNumbers.empty()){                                // if unable to open image
         __android_log_print(ANDROID_LOG_DEBUG, "LOG_TAG","error: image not read from file\n\n");         // show error message on command line
         return(0);                                                  // and exit program
     }
     cv::Mat matGrayscale;
     cv::Mat matCanny;
     cv::Mat matBlurred;
     cv::Mat matThresh;
     cv::Mat erosionMat;
     cv::Mat matThreshCopy;
    cv::cvtColor(matTestingNumbers, matGrayscale, CV_RGB2GRAY);
    //cv::GaussianBlur(matGrayscale,matBlurred,cv::Size(5, 5),0);

    cv::threshold(matGrayscale,matThresh, 0, 255, CV_THRESH_BINARY_INV | CV_THRESH_OTSU);
    //cv::adaptiveThreshold(matSub,matThresh,255,CV_ADAPTIVE_THRESH_MEAN_C,cv::THRESH_BINARY_INV,11,2);

    mRgb=matThresh;
    return 1;
    }

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_detectWords
    (JNIEnv *env, jclass, jlong addrRgba){

    return 1;
 }