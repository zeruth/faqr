FAQredux
====  

This is a work in progress revival of FAQr using a service I wrote that caches plain text game guides on demand.  
Builds (APK): https://github.com/zeruth/faqr/tags  

To load a game guide, enter a direct link to the guide into the search bar like the following example:  
```
gamefaqs.gamespot.com/ps/197343-final-fantasy-viii/faqs/51741
```
WARNING: Currently I recommend using FAQmarks on only 1 guide, as every guide will share FAQmark locations unfortunately.  
(I only worked on this to get it back for the one guide so don't expect frequent updates)  
  
If you find any issues with a certain guide link etc, create an issue on my fork please with the problem url.

This app is signed with different keys than the original FAQr, and most likely will not be able to install over it.  
If you have this problem, uninstall FAQr first,  

```

             ______ __ __ __   _   __                         ?
              || || || || ||  / \  ||                       1#!6, a           ]
              || \] || |\ || ||ø|| ||                       @#MpMN&c,        g#
              |_]   || |\\|| || || ||                      MQM#M#M*  _   , mMM
              ||    || ||\\| ||_|| || .                   4BNNMMg  pN_,ggM#M
              ||    || || || | _ | ||/|                   '`$yE&#MMMMMNB#M~
              ||    || || || || || |  |                      jNMM7#&M!jM@"
             øøøø   øø øø øø øø øø øøøø                    yQNNMNMMMMN`"F
       ______  _   __ __ ____  _    __  ____               #MNNMMMMM#' ]
        || || /_\  || || |  | /_\  /  \ ||||               lQQMMMMNM8  !
        || \]|| || |\ || '||'|| || ||\/ ||||                QMMMMMMN& j
        |_]  || || |\\||  || || || \\    \/               _g#EQMNNMWMy,
        ||   ||_|| ||\\|  || ||_||  \\   ||              rDMMBB#B QMM'
        ||   | _ | || \|  || | _ | /\||  ||             &B&2/7MMNNBM^
        ||   || || || ||  || || || \  /  ||            _NQMMNNMMQMM
       øøøø  øø øø øø øø  øø øø øø  øø   øø           NNMMNMMMMMMM#
                     __ __ __                      ,NNNMMMMMM#m_MWQ
                     || || ||                    _yPZMMM#MM']MNM MNf
                     || || ||                 ,4pN_jQ&NMMMu 4M#MMNN1
                     || || ||                 &47~`[#yMMMM&&gM#M# M&
                     || || ||               _d!     ^~~~``^`M#MM& ]M
                     ||  \v/              #/f_.              "#M&Q #
                     ||   v             _MpN`                 ]MMM&e$
                     øø   ø            gN^                    ]Q#QE_"
                                      /^                       #MM~^
                                                               ]MM~
"One born of a dragon, bearing darkness and light,              M&
  shall rise to the heavens over the still land.               _ML
     The moon's light eternal brings a promise                _4MM
       to the planet with bounty and grace."                 _N@E#e
                                                          __pMB7


```

CONFIG
======

Check res/values/default_settings.xml for default configuration


REQUIREMENTS
============

Requires - SDK Level 15  - as minimum SDK version

Currently using - SDK Level 21 - as target SDK version

@see faqr/build.gradle

... TODO more stuff here ...


CREATING A RELEASE
============================

Temporarily delete everything in /assets to reduce size of the APK

Update the versionCode and versionName in app/build.gradle

Using Android Studio > Menu > Build > Generate Signed APK...

Publish the app using the Google Play Developer Console - https://play.google.com/apps/publish


FEATURED FAQ
============

Final Fantasy IV FAQ by Johnathan 'Zy' Sawyer

http://www.gamefaqs.com/psp/615911-final-fantasy-iv-the-complete-collection/faqs/62211



eneve
-----
