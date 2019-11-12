# Vorlage für Bachelor- und Masterarbeiten

Diese LaTeX Vorlage für Bachelorarbeiten bzw. mit leichten Änderungen auch für Masterarbeiten an der Fakultät Informatik und Wirtschaftsinformatik der Hochschule für angewandte Wissenschaften Würzburg-Schweinfurt kann verändert und natürlich an die spezifischen Vorgaben der betreuenden Dozenten angepasst werden.

Mit der nächsten Version der Allgemeinen Prüfungsordnung (APO), gültig ab 01.10.2019, müssen Studierende Ihre Arbeit in zwei digitalen Fassungen abgeben. Eine Version muss den Namen des Autors enthalten, die andere Version darf den Namen des Autors nicht enthalten. Dies soll die Überprüfung auf Plagiate mit dem Tool PlagScan vereinfachen. In der LaTeX Vorlage existiert dazu eine Abfrage:

```
\ifdefined\iswithfullname
  \def\ShowBaAuthor{\BaAuthor}
\else
  \def\ShowBaAuthor{N.~N.}
\fi
```

ob die Variable `iswithfullname` auf der Kommandozeile beim Starten von PDFLaTeX gesetzt wurde oder nicht. Wenn sie gesetzt wurde, wird der volle Name des Autors angezeigt, andernfalls durch N. N. ersetzt. Wenn das
beiliegende Makefile benutzt wird, werden die zwei geforderten Versionen automatisch erzeugt. Wenn andere Werkzeuge benutzt werden, muss das Setzen dieser Variablen selbst vorgenommen werden. Notfalls, wenn alle
anderen Lösungen nicht funktionieren sollten, kommentiert man die obige Abfrage entsprechend aus und setzt die
Variable `ShowBaAuthor` selbst. Wichtig, in der Arbeit muss immer das Makro `ShowBaAuthor` verwendet werden,
wenn man den Namen des Autors angeben möchte. 

Fehlerhinweise und Verbesserungsvorschläge bitte an Peter Braun <peter.braun@fhws.de> schicken.

Veröffentlicht unter der Lizenz CC0.
