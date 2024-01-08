import axios from "axios";
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { API_URL } from '../config';
import AjoutCategorie from "./AjoutCategorie";

function WikiContent() {
    const { id } = useParams();
    const [wiki, setWiki] = useState(null);
    const navigate = useNavigate();

    const handleRetourClick = () => {
        navigate(-1);
    };

    useEffect(() => {
        searchDataWiki(id);
    }, [id]);

    const searchDataWiki = (id) => {
        axios.get(`${API_URL}/wiki/${id}/content`).then((res) => {
            setWiki(res.data);
        }).catch((error) => {
            console.error(error);
        });
    };

    const isUserAdmin = () => {
        if (localStorage.getItem('account') !== null && wiki) {
            const isAdmin = wiki.admins.some((adminId) => adminId === parseInt(localStorage.getItem('account')));
            return isAdmin;
        }
        return false;
    };

    const isUserOwner = () => {
        if (localStorage.getItem('account') !== null && wiki) {
            return parseInt(localStorage.getItem('account')) === wiki.owner;
        }
        return false;
    }

    function deleteEntry(entryId, entryName) {
      if (window.confirm("Voulez vous vraiment supprimer l'entrée " + entryName)) {
        axios.get(`${API_URL}/delete/entry/${entryId}`).then((res) => {
          if (res.status === 200) {
            window.location.reload();
          } else if (res.data.code === "409") {
            alert("Erreur lors de la suppression de l'entrée");
          }
        }).catch((error) => {
            console.error(error);
            alert("Erreur lors de la suppression de l'entrée");
        });
      }
    };

    function deleteCategory(categoryName) {
        if (window.confirm("Voulez vous vraiment supprimer la catégorie " + categoryName + " ?\n(celà supprimera TOUTES les entrées appartenant seulement a cette catégorie !)")) {
            if (window.confirm("Cette action est irréversible !\nEtes vous VRAIMENT sûr ?")) {
                axios.patch(`${API_URL}/wiki/${wiki._id}/${categoryName}/delete`).then((res) => {
                    if (res.status === 200) {
                      window.location.reload();
                    } else if (res.data.code === "409") {
                      alert("Erreur lors de la suppression de la catégorie");
                    }
                  }).catch((error) => {
                      console.error(error);
                      alert("Erreur lors de la suppression de la catégorie");
                  });
            }
        }
    };

    return (
        <div>
            <h2>Wiki {wiki?.nom || ""}</h2>
            <p>{wiki?.description || ""}</p>
            {isUserOwner() && (
                <div>
                    <Link to={`/wiki/${id}/admin`}>
                    <button style={{ cursor: 'pointer' }}>Gérer les Administrateurs</button>
                    </Link>
                    <br/>
                </div>
            )}
            {isUserAdmin() && (
                <div>
                    <h3>Ajouter une catégorie :</h3>
                    <AjoutCategorie />
                    <Link to={`/wiki/${wiki?._id || ""}/ajoutEntree`}>
                        <button style={{ cursor: 'pointer' }}>Ajouter une entrée</button>
                    </Link>
                </div>
            )}
            {wiki && wiki.categories.length === 0 ? (
                <p className="append">Aucune entrée dans le wiki.</p>
            ) : (
                wiki && wiki.categories.map((categorie) => (
                    <div key={categorie._id}>
                        <div class="flex-align">
                            <Link to={`/categorie/${wiki?._id || ""}/${categorie.nom}`}>
                                <h3 style={{ cursor: 'pointer' }}>{categorie.nom} :</h3>
                            </Link>
                            {isUserAdmin() && (
                                <div class="append">
                                    <Link to={`/wiki/${wiki?._id || ""}/category/${categorie.nom}/update`}>
                                        <button class="text-small"><b>···</b></button>
                                    </Link>
                                    <button class="text-small" onClick={() => deleteCategory(categorie.nom)}>X</button>
                                </div>
                            )}
                        </div>
                        {categorie.entrees.map((entree) => (
                            <div class="flex-align" key={entree._id}>
                                <Link to={`/entree/${entree._id}`}>
                                    <div className="append">{entree.nom}</div>
                                </Link>
                                {isUserAdmin() && (
                                    <div>
                                        <Link to={`/wiki/${wiki._id}/entry/${entree._id}/update`}>
                                            <button class="text-x-small"><b>···</b></button>
                                        </Link>
                                        <button class="text-x-small" onClick={() => deleteEntry(entree._id, entree.nom)}>X</button>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                ))
            )}
            {isUserAdmin() && (
                <div>
                    <h3>Catégories sans entrées :</h3>
                    {wiki && wiki.categoriesWithoutEntry.map((categorie) => (
                        <div class="flex-align" key={categorie}>
                            <Link to={`/categorie/${wiki?._id || ""}/${categorie}`}>
                            <h3 style={{ cursor: 'pointer' }}>{categorie}</h3>
                            </Link>
                            <div class="append">
                                <Link to={`/wiki/${wiki?._id || ""}/category/${categorie}/update`}>
                                    <button class="text-small"><b>···</b></button>
                                </Link>
                                <button class="text-small" onClick={() => deleteCategory(categorie)}>X</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
            <button style={{ cursor: 'pointer' }} onClick={handleRetourClick}>Retour</button>
        </div>
    );
}

export default WikiContent;