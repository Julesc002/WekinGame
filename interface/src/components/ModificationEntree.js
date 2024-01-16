import MDEditor from "@uiw/react-md-editor";
import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { API_URL, APP_URL } from '../config';
import BackgroundWiki from "./BackgroundWiki";

function ModificationEntree() {
    const { wikiId, entreeId } = useParams();
    const [entree, setEntree] = useState([]);
    const [entreeUpdate, setEntreeUpdate] = useState([]);
    const [categories, setCategories] = useState([]);
    const [entreeNom, setEntreeNom] = useState("");
    const [categoriesForEntree, setCategoriesForEntree] = useState([]);
    const [entreeDonnees, setEntreeDonnees] = useState([]);
    const [errorMessage, setErrorMessage] = useState("");
    const navigate = useNavigate();

    const handleRetourClick = () => {
        navigate(-1);
    };

    useEffect(() => {
        getEntree(entreeId);
    }, [entreeId]);

    const getEntree = (id) => {
        axios.get(`${API_URL}/entry/` + id).then((res) => {
            setEntree(res.data);
        });
    };

    useEffect(() => {
        getCategories(wikiId);
    }, [wikiId]);

    const getCategories = (id) => {
        axios.get(`${API_URL}/wiki/` + id).then((res) => {
            setCategories(res.data.categories);
        });
    };

    useEffect(() => {
        if (entree && Object.keys(entree).length !== 0) {
            setEntreeNom(entree.nom);
            setCategoriesForEntree([...entree.categories]);
            setEntreeDonnees([...entree.donnees]);
        }
    }, [entree]);

    useEffect(() => {
        const majEntree = {
          nom: entreeNom,
          id_wiki: wikiId,
          categories: categoriesForEntree,
          donnees: entreeDonnees.map((donnee) => ({
            titre: donnee.titre,
            contenu: donnee.contenu
          }))
        };
        setEntreeUpdate(majEntree);
    }, [entreeNom, wikiId, categoriesForEntree, entreeDonnees]);

    const updateEntree = () => {
        const checkContentDonnees = entreeDonnees.some(function (donnee) {
            return donnee.titre.trim().length === 0 || donnee.contenu.trim().length === 0;
        });
    
        if (entreeNom.trim().length === 0) {
            setErrorMessage("Veuillez donner un titre à votre entrée");
        } else if (categoriesForEntree.length === 0) {
            setErrorMessage("Veuillez sélectionner une catégorie");
        } else if (entreeDonnees.length === 0) {
            setErrorMessage("Veuillez ajouter une donnée à votre entrée");
        } else if (checkContentDonnees) {
            setErrorMessage("Veuillez compléter les champs de votre/vos donnee(s)");
        } else {
            setErrorMessage("");
            axios.put(API_URL + '/modify/entry/' + entreeId, entreeUpdate).then((response) => {
                if (response.status === 200) {
                    window.location.href = `${APP_URL}/wiki/${wikiId}`;
                } else if (response.status === "409") {
                    alert("Erreur lors de la modification de l'entrée");
                }
            });
        }
    };

    const majNameEntree = (e) => {
        setEntreeNom(e.target.value);
    };

    const handleCheckboxChange = (event) => {
        const { name, checked } = event.target;
        if (checked) {
          setCategoriesForEntree((prevCategories) => [...prevCategories, name]);
        } else {
          setCategoriesForEntree((prevCategories) => prevCategories.filter((item) => item !== name));
        }
    };

    const handleMajDonneeTitle = (e, index) => {
        const donneesCopie = [...entreeDonnees];
        donneesCopie[index].titre = e.target.value;
        setEntreeDonnees(donneesCopie);
    };

    const handleSupprDonnee = (index) => {
        const donneesCopie = [...entreeDonnees];
        donneesCopie.splice(index, 1);
        setEntreeDonnees(donneesCopie);
    };

    const handleMajDonneeContent = (e, index) => {
        const donneesCopie = [...entreeDonnees];
        donneesCopie[index].contenu = e;
        setEntreeDonnees(donneesCopie);
    };

    const handleAjoutDonnee = () => {
        const nouvelleDonnee = ["", ""];
        const donneesCopie = [...entreeDonnees];
        donneesCopie.push(nouvelleDonnee);
        setEntreeDonnees(donneesCopie);
    };
    const renderMarkdownEditor = (index) => (
        <MDEditor
            height={200}
            value={entreeDonnees[index].contenu}
            onChange={(e) => handleMajDonneeContent(e, index)}
        />
    );

    const handleUpDataIndex = (index) => {
        const updatedData = [...entreeDonnees];
        const data = updatedData[index];
        updatedData[index] = updatedData[index - 1];
        updatedData[index - 1] = data;
        setEntreeDonnees(updatedData);
    };

    const handleDownDataIndex = (index) => {
        const updatedData = [...entreeDonnees];
        const data = updatedData[index];
        updatedData[index] = updatedData[index + 1];
        updatedData[index + 1] = data;
        setEntreeDonnees(updatedData);
    };

    return (
        <div class="contenuWiki">
            <BackgroundWiki id={wikiId} />
            {entree && (
                <div>
                    <h2>Modification de l'entrée</h2>
                    <label>
                        Nom de l'entrée :
                        <input type="text" value={entreeNom} placeholder='Ex: "Master Sword"' onChange={(e) => majNameEntree(e)}/>
                    </label>
                    <label>
                        Catégorie :
                        {categories && categories.map(function (categorie) {
                        const isChecked = categoriesForEntree.includes(categorie);
                        return (
                            <div class="append">
                                <input type="checkbox" name={categorie} onChange={handleCheckboxChange} checked={isChecked} />
                                <label for={categorie}>{categorie}</label>
                            </div>
                        );
                    })}
                    </label>
                    <label>
                        Donnée·s :
                        {entreeDonnees && entreeDonnees.map(function (donnee, index) {
                            return (
                                <div key={index} class="box-content flex-down">
                                        <div class="flex-spaced">
                                            <input type="text" placeholder="Titre" value={entreeDonnees[index].titre} onChange={(e) => handleMajDonneeTitle(e, index)} />
                                            <button class="float-right" onClick={() => handleSupprDonnee(index)}>x</button>
                                        </div>
                                        {renderMarkdownEditor(index)}
                                        <div>
                                            {index !== 0 && <button onClick={() => handleUpDataIndex(index)}>↑</button>}
                                            {index !== entreeDonnees.length - 1 && <button onClick={() => handleDownDataIndex(index)}>↓</button>}
                                        </div>
                                </div>
                            );
                        })}
                        <button onClick={handleAjoutDonnee}>Ajouter une donnée</button>
                    </label>
                    <br/>
                    <div>
                        <button onClick={handleRetourClick}>Annuler</button>
                        <button class="button-highlight" onClick={updateEntree}>Valider l'entrée</button>
                    </div>
                    <p>{errorMessage}</p>
                </div>
            )}
        </div>
    );
    
}

export default ModificationEntree;