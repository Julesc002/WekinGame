import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_URL, APP_URL } from '../config';
import MDEditor from "@uiw/react-md-editor";

function AjoutEntree() {
    const { id } = useParams();
    const [entree, setEntree] = useState();
    const [name, setName] = useState("");
    const [categories, setCategories] = useState([]);
    const [categoriesForEntree, setCategoriesForEntree] = useState([]);
    const [donnees, setDonnees] = useState([]);
    const [errorMessage, setErrorMessage] = useState("");
    const navigate = useNavigate();

    const [markdownContent, setMarkdownContent] = useState([]);

    const handleRetourClick = () => {
        navigate(-1);
    };

    const majNameEntree = (e) => {
        setName(e.target.value);
    };
    const renderMarkdownEditor = (index) => (
        <MDEditor
            height={200}
            value={markdownContent[index]}
            onChange={(e) => handleMajDonneeContent(e, index)}
        />
    );

    useEffect(() => {
        const majEntree = {
          nom: name,
          id_wiki: id,
          categories: categoriesForEntree,
          donnees: donnees.map((donnee) => ({
            titre: donnee[0],
            contenu: donnee[1]
          }))
        };
        setEntree(majEntree);
      }, [name, id, categoriesForEntree, donnees]);
      

    const addEntree = () => {
        const checkContentDonnees = donnees.some(function (donnee) {
            return donnee[0].trim().length === 0 || donnee[1].size === 0;
        });
    
        if (name.trim().length === 0) {
            setErrorMessage("Veuillez donner un titre à votre entrée");
        } else if (categoriesForEntree.length === 0) {
            setErrorMessage("Veuillez sélectionner une catégorie");
        } else if (donnees.length === 0) {
            setErrorMessage("Veuillez ajouter une donnée à votre entrée");
        } else if (checkContentDonnees) {
            setErrorMessage("Veuillez compléter les champs de votre/vos donnee(s)");
        } else {
            setErrorMessage("");
            axios.post(API_URL + '/create/entry', entree).then((response) => {
                if (response.status === 200) {
                    window.location.href = `${APP_URL}/wiki/${id}`;
                } else if (response.data.code === "409") {
                    alert("Erreur lors de la création de l'entrée");
                }
            });
        }
    };

    const handleCheckboxChange = (event) => {
        const { name, checked } = event.target;
        if (checked) {
          setCategoriesForEntree((prevCategories) => [...prevCategories, name]);
        } else {
          setCategoriesForEntree((prevCategories) => prevCategories.filter((item) => item !== name));
        }
    };

    const handleAjoutDonnee = () => {
        const nouvelleDonnee = ["", ""];
        const donneesCopie = [...donnees];
        donneesCopie.push(nouvelleDonnee);
        setDonnees(donneesCopie);
    };

    const handleSupprDonnee = (index) => {
        const donneesCopie = [...donnees];
        donneesCopie.splice(index, 1);
        setDonnees(donneesCopie);
    };

    const handleMajDonneeTitle = (e, index) => {
        const donneesCopie = [...donnees];
        donneesCopie[index][0] = e.target.value;
        setDonnees(donneesCopie);
    };

    const handleMajDonneeContent = (e, index) => {
        const mdContent = [...markdownContent];
        const donneesCopie = [...donnees];
        donneesCopie[index][1] = e;
        mdContent[index]=e;
        setDonnees(donneesCopie);
        setMarkdownContent(mdContent);
    };

    useEffect(() => {
        getCategories(id);
    });

    const getCategories = (id) => {
        axios.get(`${API_URL}/wiki/` + id).then((res) => {
            setCategories(res.data.categories);
        });
    };

    return (
        <div class="flex-down">
            <h2>Nouvelle entrée</h2>
            <label>
                Nom de l'entrée :
                <input type="text" placeholder='Ex: "Master Sword"' onChange={(e) => majNameEntree(e)} />
            </label>
            <label>
                Catégorie :
                {categories && categories.map(function (categorie) {
                    return (
                        <div class="append">
                            <input type="checkbox" name={categorie} onChange={handleCheckboxChange}/>
                            <label for={categorie}>{categorie}</label>
                        </div>
                    );
                })}
            </label>
            <label>
                Donnée·s :
                {donnees && donnees.map(function (donnee, index) {
                    return (
                        <div key={index} class="small-box-content flex-down">
                                <div class="flex-spaced">
                                    <input type="text" placeholder="Titre" value={donnees[index][0]} onChange={(e) => handleMajDonneeTitle(e, index)} />
                                    <button class="float-right" onClick={() => handleSupprDonnee(index)}>x</button>
                                </div>
                                {renderMarkdownEditor(index)}
                        </div>
                    );
                })}
                <button onClick={handleAjoutDonnee}>Ajouter une donnée</button>
            </label>
            <br/>
            <div>
                <button onClick={handleRetourClick}>Annuler</button>
                <button class="button-highlight" onClick={addEntree}>Valider l'entrée</button>
            </div>
            <p>{errorMessage}</p>
        </div>
    );
}

export default AjoutEntree;