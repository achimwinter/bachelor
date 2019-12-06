# Zur Vorlage

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

 # Zur Bachelorarbeit

 Ziel der Arbeit ist eine vereinfachte Nutzung von private Keys.